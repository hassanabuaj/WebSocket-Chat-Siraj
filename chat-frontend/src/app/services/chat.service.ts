import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Message } from '../models/message';
import { ConfigService } from './config.service';

@Injectable({ providedIn: 'root' })
/**
 * שירות צ'אט: קריאות REST להיסטוריית הודעות/איתור משתמשים,
 * התחברות ל־WebSocket ושליחת הודעות בזמן אמת.
 */
export class ChatService {
  private ws?: WebSocket;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private config: ConfigService
  ) {}

  /**
   * מחזיר היסטוריית הודעות מול משתמש נתון.
   * @param withUser מזהה המשתמש השני
   * @param limit מספר הודעות מירבי להחזרה
   */
  async getHistory(withUser: string, limit = 50) {
    const token = await this.auth.user?.getIdToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http
      .get<Message[]>(
        `${this.config.apiBaseUrl}/api/messages?withUser=${encodeURIComponent(
          withUser
        )}&limit=${limit}`,
        { headers }
      )
      .toPromise();
  }

  /**
   * יוצר/מחדש חיבור WebSocket ומחזיר פרומיס שנפטר כאשר החיבור נפתח.
   */
  async connect(): Promise<WebSocket> {
    // (Re)connect only if not already open
    if (this.ws && this.ws.readyState === WebSocket.OPEN) return this.ws;
    const token = await this.auth.user?.getIdToken();
    if (!token) throw new Error('Not logged in');
    this.ws = new WebSocket(`${this.config.wsUrl}/ws/chat?token=${token}`);
    return new Promise((resolve, reject) => {
      this.ws!.onopen = () => resolve(this.ws!);
      this.ws!.onerror = (e) => reject(e);
    });
  }

  /**
   * רושם מאזין להודעות נכנסות מה־WebSocket.
   * @param handler פונקציה המטפלת בהודעה מסוג Message
   */
  onMessage(handler: (msg: Message) => void) {
    if (!this.ws) throw new Error('WS not connected yet');
    this.ws.onmessage = (e) => {
      try {
        handler(JSON.parse(e.data));
      } catch {
        /* ignore parse errors */
      }
    };
  }

  /**
   * שולח הודעת צ'אט דרך WebSocket.
   */
  send(msg: Message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN)
      throw new Error('WebSocket not open');
    this.ws.send(JSON.stringify(msg));
  }

  /**
   * חיפוש משתמש לפי אימייל.
   * @returns פרטי משתמש או null במקרה של שגיאה/לא נמצא
   */
  async findUserByEmail(
    email: string
  ): Promise<{ uid: string; email: string } | null> {
    const token = await this.auth.user?.getIdToken();
    const res = await fetch(
      `${this.config.apiBaseUrl}/api/users/resolve?email=${encodeURIComponent(
        email
      )}`,
      {
        headers: {
          Authorization: `Bearer ${token ?? ''}`,
          'ngrok-skip-browser-warning': 'true',
        },
      }
    );
    if (!res.ok) return null;
    return res.json();
  }

  /**
   * מחזיר רשימת שיחות אחרונות של המשתמש הנוכחי.
   */
  async getRecentConversations(): Promise<
    import('../models/conversation').ConversationSummary[]
  > {
    const token = await this.auth.user?.getIdToken();
    const res = await fetch(
      `${this.config.apiBaseUrl}/api/conversations/recent?limit=20`,
      {
        headers: {
          Authorization: `Bearer ${token ?? ''}`,
          'ngrok-skip-browser-warning': 'true',
        },
      }
    );
    if (!res.ok) return [];
    return res.json();
  }

  /**
   * מאתר משתמש לפי אימייל או UID דרך ה־backend ומטיל שגיאות ייעודיות על פי סטטוס.
   * @throws שגיאה עם code: 'SELF' | 'NOT_FOUND' | 'RESOLVE_FAILED' | 'BAD_REQUEST'
   */
  async resolveUser(input: {
    email?: string;
    uid?: string;
  }): Promise<{ uid: string; email: string } | null> {
    const token = await this.auth.user?.getIdToken();
    const params = input.email
      ? `email=${encodeURIComponent(input.email)}`
      : `uid=${encodeURIComponent(input.uid!)}`;
    const res = await fetch(
      `${this.config.apiBaseUrl}/api/users/resolve?${params}`,
      {
        headers: {
          Authorization: `Bearer ${token ?? ''}`,
          'ngrok-skip-browser-warning': 'true',
        },
      }
    );
    if (res.ok) return res.json();
    // Map backend validation and not-found to explicit errors
    if (res.status === 400) {
      try {
        const body = await res.json();
        const msg = String(body?.error || 'Invalid request');
        const err: any = new Error(
          msg.toLowerCase().includes('yourself')
            ? 'Cannot start conversation with yourself'
            : msg
        );
        err.code = msg.toLowerCase().includes('yourself') ? 'SELF' : 'BAD_REQUEST';
        throw err;
      } catch (e) {
        const err: any = new Error('Invalid request');
        err.code = 'BAD_REQUEST';
        throw err;
      }
    }
    if (res.status === 404) {
      const err: any = new Error('NOT_FOUND');
      err.code = 'NOT_FOUND';
      throw err;
    }
    const err: any = new Error('RESOLVE_FAILED');
    err.code = 'RESOLVE_FAILED';
    throw err;
  }

  /**
   * סוגר את חיבור ה־WebSocket אם פתוח.
   */
  close() {
    if (this.ws) {
      this.ws.close();
      this.ws = undefined;
    }
  }
}
