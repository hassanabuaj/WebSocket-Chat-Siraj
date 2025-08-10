import {
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
  ElementRef,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../services/auth.service';
import { Message } from '../../models/message';
import { ConversationSummary } from '../../models/conversation';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="layout" *ngIf="svc.user as u; else loginFirst">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="side-header">Recent</div>
        <div class="side-list">
          <button
            class="side-item"
            *ngFor="let c of recent()"
            [class.active]="c.otherUid === otherUid"
            (click)="openConversation(c)"
          >
            <div class="avatar">{{ initials(c.otherEmail || c.otherUid) }}</div>
            <div class="meta">
              <div class="name">
                {{ c.otherEmail || (c.otherUid | slice : 0 : 10) + '…' }}
              </div>
              <div class="time">
                {{
                  c.lastTimestampIso
                    ? (c.lastTimestampIso | date : 'shortTime')
                    : ''
                }}
              </div>
            </div>
            <span class="unread" *ngIf="unreadCount(c.otherUid) > 0">
              {{ unreadCount(c.otherUid) }}
            </span>
          </button>
        </div>
        <div class="side-footer">
          <button class="btn small" (click)="refreshRecent()">Refresh</button>
        </div>
      </aside>

      <!-- Main panel -->
      <section class="panel">
        <header class="chat-header">
          <div class="user">
            <div class="avatar me">{{ initials(u.uid) }}</div>
            <div class="meta">
              <div class="title">You</div>
              <div class="uid">{{ u.uid }}</div>
            </div>
          </div>
          <div class="status">
            <span class="dot" [class.on]="wsOpen()"></span>
            <span class="badge" [class.open]="wsOpen()"
              >WS: {{ wsOpen() ? 'Open' : 'Closed' }}</span
            >
            <button class="btn small danger" (click)="logout()">Logout</button>
          </div>
        </header>

        <!-- Controls -->
        <div class="controls">
          <div class="row">
            <input
              [(ngModel)]="otherEmail"
              placeholder="Chat with email e.g. someone@example.com"
              (keyup.enter)="resolveAndLoad()"
            />
            <button class="btn" (click)="resolveAndLoad()">Load</button>
            <button
              class="btn"
              (click)="resolveAndConnect()"
              [disabled]="wsOpen()"
            >
              Connect
            </button>
          </div>
          <p class="hint">Enter: send • Shift+Enter: newline</p>
        </div>

        <!-- History (only scrollable area) -->
        <div class="history" #historyBox>
          <ng-container *ngFor="let m of messages()">
            <div class="bubble-row" [class.me]="m.senderId === u.uid">
              <div
                class="avatar"
                [class.them-avatar]="m.senderId !== u.uid"
                [class.me-avatar]="m.senderId === u.uid"
              >
                {{
                  initials(
                    m.senderId === u.uid ? u.uid : otherEmail || otherUid
                  )
                }}
              </div>

              <!-- The bubble auto-sizes to content (inline-block) and never stretches full-width -->
              <div class="bubble">
                <div class="text">{{ m.message }}</div>
                <div class="ts">{{ m.timestamp | date : 'MMM d, HH:mm' }}</div>
              </div>
            </div>
          </ng-container>
        </div>

        <!-- Composer -->
        <footer class="composer">
          <textarea
            [(ngModel)]="draft"
            [disabled]="!wsOpen()"
            placeholder="Type a message..."
            (keydown)="onKeyDown($event)"
          ></textarea>

          <button
            class="btn primary"
            (click)="send()"
            [disabled]="!wsOpen() || !canSend()"
          >
            Send
          </button>
        </footer>

        <p class="error" *ngIf="error()">{{ error() }}</p>
      </section>
    </div>

    <ng-template #loginFirst>
      <div class="empty">Please log in first (go to /)</div>
    </ng-template>
  `,
  styles: [
    `
      :host {
        display: block;
        height: 100%;
      }

      .layout {
        --bg: linear-gradient(180deg, #0c1224 0%, #0a122b 100%);
        --panel: rgba(17, 24, 41, 0.85);
        --panel-2: rgba(13, 20, 36, 0.85);
        --panel-border: #223055;
        --text: #e8ecf1;
        --muted: #9fb0c7;
        --accent: #5b8cff;
        --accent-2: #8ec5ff;
        --bubble-me: linear-gradient(135deg, #316bff, #4f8cff);
        --bubble-them: rgba(27, 36, 56, 0.9);

        height: 100svh;
        max-height: 100svh;
        width: 100%;
        display: grid;
        grid-template-columns: 280px 1fr;
        gap: 14px;
        padding: 14px;
        background: var(--bg);
        color: var(--text);
        overflow: hidden;
        backdrop-filter: blur(6px);
      }
      .sidebar,
      .panel,
      .history {
        min-height: 0;
      }

      /* Sidebar */
      .sidebar {
        background: var(--panel);
        border: 1px solid var(--panel-border);
        border-radius: 16px;
        display: grid;
        grid-template-rows: auto 1fr auto;
        overflow: hidden;
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
      }
      .side-header,
      .side-footer {
        padding: 12px 14px;
        border-bottom: 1px solid var(--panel-border);
        background: linear-gradient(180deg, rgba(255,255,255,0.03), rgba(255,255,255,0));
      }
      .side-footer {
        border: 0;
        border-top: 1px solid var(--panel-border);
      }
      .side-list {
        overflow: auto;
      }
      .side-item {
        width: 100%;
        border: 0;
        background: transparent;
        color: var(--text);
        padding: 10px 12px;
        display: flex;
        gap: 12px;
        align-items: center;
        cursor: pointer;
        transition: background 0.2s ease, transform 0.05s ease;
      }
      .side-item:hover {
        background: rgba(23, 34, 59, 0.8);
      }
      .side-item.active {
        background: rgba(27, 38, 69, 0.95);
        box-shadow: inset 0 0 0 1px var(--panel-border);
      }
      .side-item .unread {
        margin-left: auto;
        background: linear-gradient(135deg, #ff5a6b, #ff7d7c);
        color: white;
        border-radius: 999px;
        padding: 2px 8px;
        font-size: 12px;
        min-width: 18px;
        text-align: center;
        box-shadow: 0 6px 16px rgba(255, 90, 107, 0.35);
      }
      .avatar {
        width: 38px;
        height: 38px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: radial-gradient(ellipse at top, #20315e, #1a2747);
        color: #cfe0ff;
        font-weight: 700;
        flex: 0 0 auto;
        border: 1px solid var(--panel-border);
      }
      .me-avatar {
        background: radial-gradient(ellipse at top, #2a52b2, #244599);
        color: #e8f0ff;
      }
      .them-avatar {
        background: radial-gradient(ellipse at top, #243252, #1f2b45);
        color: #d7e6ff;
      }
      .meta {
        overflow: hidden;
      }
      .name {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .time {
        color: var(--muted);
        font-size: 12px;
      }

      /* Main panel */
      .panel {
        display: grid;
        grid-template-rows: auto auto 1fr auto auto;
        gap: 10px;
        overflow: hidden;
      }
      .chat-header {
        background: var(--panel);
        border: 1px solid var(--panel-border);
        border-radius: 16px;
        padding: 10px 14px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        box-shadow: 0 10px 28px rgba(0, 0, 0, 0.25);
      }
      .user {
        display: flex;
        gap: 12px;
        align-items: center;
      }
      .meta .title {
        font-weight: 700;
        letter-spacing: 0.2px;
      }
      .meta .uid {
        color: var(--muted);
        font-size: 12px;
      }
      .status .badge {
        padding: 6px 10px;
        border-radius: 999px;
        font-size: 12px;
        background: rgba(51, 64, 100, 0.6);
        color: #d6def0;
        border: 1px solid #43507a;
        margin-left: 8px;
      }
      .status .badge.open {
        background: rgba(25, 57, 111, 0.8);
        color: #d9e7ff;
        border-color: #2a5fb6;
      }
      .status .dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #7a879d;
        display: inline-block;
        box-shadow: 0 0 0 2px rgba(122,135,157,0.2);
      }
      .status .dot.on {
        background: #38d39f;
        box-shadow: 0 0 0 4px rgba(56,211,159,0.15);
      }

      .controls {
        background: var(--panel);
        border: 1px solid var(--panel-border);
        border-radius: 16px;
        padding: 10px 12px;
        box-shadow: 0 10px 24px rgba(0, 0, 0, 0.2);
      }
      .row {
        display: flex;
        gap: 10px;
        align-items: center;
      }
      .row input {
        flex: 1;
        min-width: 0;
        padding: 10px 12px;
        background: var(--panel-2);
        color: var(--text);
        border: 1px solid var(--panel-border);
        border-radius: 12px;
        outline: none;
        transition: border-color 0.2s ease, box-shadow 0.2s ease;
      }
      .row input:focus {
        border-color: var(--accent);
        box-shadow: 0 0 0 3px rgba(91, 140, 255, 0.2);
      }
      .hint {
        margin: 4px 2px 0 2px;
        color: var(--muted);
        font-size: 12px;
      }

      .btn {
        background: linear-gradient(135deg, #2a3a62, #2a3656);
        color: #e9f0ff;
        border: 1px solid #33406a;
        padding: 8px 12px;
        border-radius: 12px;
        cursor: pointer;
        white-space: nowrap;
        transition: transform 0.05s ease, box-shadow 0.2s ease, filter 0.2s ease;
      }
      .btn.small {
        padding: 6px 10px;
        font-size: 12px;
      }
      .btn.danger {
        background: linear-gradient(135deg, #c54242, #a53131);
        border-color: #a23b3b;
      }
      .btn:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }
      .btn.primary {
        background: linear-gradient(135deg, #486efc, #5b8cff);
        border-color: #2b57c9;
        color: white;
        box-shadow: 0 10px 24px rgba(91, 140, 255, 0.25);
      }
      .btn:hover:not(:disabled) {
        filter: brightness(1.05);
      }
      .btn:active:not(:disabled) {
        transform: translateY(1px);
      }

      .history {
        background: var(--panel);
        border: 1px solid var(--panel-border);
        border-radius: 16px;
        padding: 12px;
        overflow: auto; /* ONLY this scrolls */
        box-shadow: inset 0 0 0 1px var(--panel-border);
        scroll-behavior: smooth;
      }

      /* Bubbles: auto-size to content (inline-block) with max-width cap */
      .bubble-row {
        display: grid;
        grid-template-columns: 38px auto;
        gap: 10px;
        align-items: end;
        margin: 8px 0;
      }
      .bubble-row.me {
        grid-template-columns: auto 38px;
      }
      .bubble-row.me .avatar {
        order: 2;
      }
      .bubble-row.me .bubble {
        order: 1;
        justify-self: end;
        background: var(--bubble-me);
        border: 1px solid rgba(86, 133, 255, 0.5);
      }

      .bubble {
        display: inline-block; /* key: size to content */
        max-width: min(72%, 62ch); /* cap very long messages */
        padding: 10px 12px;
        border-radius: 14px;
        color: #f7faff;
        background: var(--bubble-them);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
        border: 1px solid var(--panel-border);
        white-space: pre-wrap;
        word-wrap: break-word;
        justify-self: start; /* prevent stretch on receiver side */
      }
      .bubble .ts {
        margin-top: 6px;
        font-size: 11px;
        color: #d2dcff99;
        text-align: right;
      }

      .composer {
        background: var(--panel);
        border: 1px solid var(--panel-border);
        border-radius: 16px;
        padding: 10px;
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 10px;
        align-items: center;
        box-shadow: 0 10px 24px rgba(0, 0, 0, 0.2);
      }
      .composer textarea {
        min-height: 44px;
        max-height: 160px;
        resize: vertical;
        padding: 10px 12px;
        border-radius: 12px;
        background: var(--panel-2);
        color: var(--text);
        border: 1px solid var(--panel-border);
        outline: none;
        line-height: 1.35;
        font-family: inherit;
        transition: border-color 0.2s ease, box-shadow 0.2s ease;
      }
      .composer textarea:focus {
        border-color: var(--accent);
        box-shadow: 0 0 0 3px rgba(91, 140, 255, 0.2);
      }

      .error {
        color: #ff9aa2;
        margin: 2px 2px 0;
        text-align: center;
      }

      @media (max-width: 940px) {
        .layout {
          grid-template-columns: 1fr;
          padding: 10px;
        }
        .sidebar {
          display: none;
        }
      }
    `,
  ],
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('historyBox') historyBox?: ElementRef<HTMLDivElement>;

  otherUid = '';
  otherEmail = '';
  draft = '';

  messages = signal<Message[]>([]);
  recent = signal<ConversationSummary[]>([]);
  error = signal<string>('');
  wsOpen = signal<boolean>(false);

  // Cache of messages per conversation and unread counters
  private messagesByUser = new Map<string, Message[]>();
  private unreadByUser = new Map<string, number>();

  constructor(
    private chat: ChatService,
    public svc: AuthService,
    private router: Router
  ) {}

  /**
   * מאותחל בעת עליית הקומפוננטה - טוען רשימת שיחות אחרונות.
   */
  ngOnInit(): void {
    this.refreshRecent();
  }
  /**
   * נסגר בעת הריסת הקומפוננטה - סוגר חיבורי WebSocket פתוחים.
   */
  ngOnDestroy(): void {
    this.chat.close();
  }

  /**
   * טוען שיחות אחרונות מהשרת ומעדכן את הרשימה בצד לקוח.
   */
  async refreshRecent() {
    try {
      this.recent.set(await this.chat.getRecentConversations());
    } catch {}
  }

  initials(s: string | undefined | null) {
    if (!s) return '??';
    const slim = s.replace(/[^A-Za-z0-9]/g, '');
    return slim.slice(0, 2).toUpperCase();
  }

  /**
   * פותח שיחה עם המשתמש שנבחר בצד שמאל ומנקה מונה שלא נקראו.
   */
  async openConversation(c: ConversationSummary) {
    this.otherUid = c.otherUid;
    this.otherEmail = c.otherEmail ?? '';
    // Clear unread badge for this conversation
    this.unreadByUser.set(this.otherUid, 0);
    await this.resolveAndLoad();
  }

  /**
   * מבצע גלילה לתחתית היסטוריית השיחה לאחר ציור ה־DOM.
   */
  private scrollToBottomSoon() {
    const doScroll = () => {
      const el = this.historyBox?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    };
    requestAnimationFrame(() => requestAnimationFrame(doScroll));
  }

  /**
   * מאתר משתמש לפי אימייל (אם נדרש), טוען היסטוריית הודעות לשיחה הנוכחית,
   * ומציג אותה. מטפל בשגיאות עצמיות/לא נמצא בהתאם.
   */
  async resolveAndLoad() {
    this.error.set('');
    try {
      if (!this.otherUid && this.otherEmail) {
        const r = await this.chat.resolveUser({
          email: this.otherEmail.trim(),
        });
        if (!r) throw new Error('User not found');
        this.otherUid = r.uid;
      }
      if (!this.otherUid) throw new Error('Set email or select a recent chat');

      const list =
        (await this.chat.getHistory(this.otherUid.trim(), 200)) || [];
      list.sort(
        (a, b) =>
          new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
      );
      this.messages.set(list);
      this.scrollToBottomSoon();
    } catch (e: any) {
      const code = e?.code as string | undefined;
      const msg = (e?.message as string | undefined) ?? String(e);
      if (code === 'SELF' || msg.toLowerCase().includes('yourself')) {
        this.error.set('Cannot start conversation with yourself');
        this.otherEmail = '';
        return;
      }
      if (code === 'NOT_FOUND' || msg.toLowerCase().includes('not found')) {
        this.error.set('User not found');
        return;
      }
      this.error.set('Failed to resolve user');
    }
  }

  /**
   * מתחבר ל־WebSocket ומאזין להודעות נכנסות; מציג רק הודעות שייכות לשיחה הנוכחית.
   */
  async resolveAndConnect() {
    this.error.set('');
    try {
      await this.resolveAndLoad(); // loads history first
      await this.chat.connect();
      this.wsOpen.set(true);
      this.chat.onMessage((m) => {
        const me = this.svc.user?.uid;
        if (!me) return;
        const partnerId = m.senderId === me ? m.receiverId : m.senderId;

        // Update cache for that partner
        const existing = this.messagesByUser.get(partnerId) ?? [];
        const updated = [...existing, m];
        updated.sort(
          (a, b) =>
            new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
        );
        this.messagesByUser.set(partnerId, updated);

        if (partnerId === this.otherUid) {
          // Show in open chat only if it belongs to it
          this.messages.set(updated);
          this.scrollToBottomSoon();
        } else {
          // Otherwise, increment unread for that conversation
          const prev = this.unreadByUser.get(partnerId) ?? 0;
          this.unreadByUser.set(partnerId, prev + 1);
        }
      });
    } catch (e: any) {
      this.error.set(e?.message ?? String(e));
    }
  }

  /**
   * בדיקה אם ניתן לשלוח הודעה (יש טקסט ומשתמש יעד).
   */
  canSend() {
    return !!this.draft.trim() && !!this.otherUid;
  }

  /**
   * שולח הודעה לשיחה הנוכחית דרך WebSocket תוך מניעת שליחת הודעה לעצמי.
   */
  async send() {
    this.error.set('');
    try {
      const me = this.svc.user?.uid;
      if (!me) throw new Error('Not logged in');
      if (!this.otherUid) throw new Error('Choose a user (email or recent)');
      if (this.otherUid === me) throw new Error('You cannot chat with yourself');
      if (!this.draft.trim()) return;

      const msg: Message = {
        senderId: me,
        receiverId: this.otherUid.trim(),
        timestamp: new Date().toISOString(),
        message: this.draft,
      };
      this.chat.send(msg);
      this.draft = '';
    } catch (e: any) {
      this.error.set(e?.message ?? String(e));
    }
  }

  /**
   * מאזין למקש Enter כדי לשלוח הודעה במהירות (Shift+Enter ירד שורה).
   */
  onKeyDown(ev: KeyboardEvent) {
    if (ev.key === 'Enter' && !ev.shiftKey) {
      ev.preventDefault();
      if (this.wsOpen() && this.canSend()) this.send();
    }
  }

  /**
   * מחזיר את מספר ההודעות שלא נקראו עבור משתמש יעד מסוים.
   */
  unreadCount(uid: string): number {
    return this.unreadByUser.get(uid) ?? 0;
  }

  /**
   * מבצע יציאה, סוגר WS, מנקה מצב ומנווט חזרה למסך ההתחברות.
   */
  async logout() {
    this.error.set('');
    try {
      await this.svc.logout();
      this.chat.close();
      this.wsOpen.set(false);
      this.otherUid = '';
      this.otherEmail = '';
      this.messages.set([]);
      await this.router.navigate(['/']);
    } catch (e: any) {
      this.error.set(e?.message ?? String(e));
    }
  }
}
