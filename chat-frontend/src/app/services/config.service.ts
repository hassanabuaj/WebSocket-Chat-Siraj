import { Injectable } from '@angular/core';

export interface RuntimeConfig {
  apiBaseUrl: string; // e.g., https://<ngrok-domain>
  wsUrl: string; // e.g., wss://<ngrok-domain>
}

@Injectable({ providedIn: 'root' })
/**
 * שירות טעינת קונפיגורציית ריצה (Runtime) מהקובץ `runtime-config.json`.
 * מאפשר לכוון את כתובות ה־API וה־WebSocket בזמן פריסה ללא צורך בבנייה מחדש.
 */
export class ConfigService {
  private config?: RuntimeConfig;

  /**
   * טוען את קובץ הקונפיגורציה מהנתיב `/runtime-config.json`.
   * אם הטעינה נכשלה, נופל חזרה לברירת מחדל ל־localhost.
   */
  async load(): Promise<void> {
    try {
      const res = await fetch('/runtime-config.json', {
        cache: 'no-store',
        headers: { 'ngrok-skip-browser-warning': 'true' },
      });
      if (!res.ok) throw new Error('runtime-config.json not found');
      this.config = (await res.json()) as RuntimeConfig;
    } catch {
      // Fallback for local dev
      this.config = {
        apiBaseUrl: 'http://localhost:8080',
        wsUrl: 'ws://localhost:8080',
      };
    }
  }

  /**
   * מחזיר את כתובת הבסיס ל־API (ללא סלאש מסיים).
   */
  get apiBaseUrl(): string {
    if (!this.config) throw new Error('Config not loaded');
    return this.config.apiBaseUrl.replace(/\/$/, '');
  }

  /**
   * מחזיר את כתובת ה־WebSocket (ללא סלאש מסיים).
   */
  get wsUrl(): string {
    if (!this.config) throw new Error('Config not loaded');
    return this.config.wsUrl.replace(/\/$/, '');
  }
}


