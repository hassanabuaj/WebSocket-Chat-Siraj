import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigService } from './config.service';

import { initializeApp } from 'firebase/app';
import {
  getAuth,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
  User,
} from 'firebase/auth';

@Injectable({ providedIn: 'root' })
/**
 * שירות אימות המבוסס על Firebase Authentication.
 * אחראי לניהול משתמש מחובר, קבלת טוקן, כניסה ויציאה, וסנכרון המשתמש לשרת.
 */
export class AuthService {
  private app = initializeApp(environment.firebase);
  private auth = getAuth(this.app);

  user: User | null = null;
  idToken: string | null = null;

  /**
   * מאזין לשינויי סטטוס התחברות ומעדכן משתמש/טוקן בזיכרון.
   */
  constructor(private cfg: ConfigService) {
    onAuthStateChanged(this.auth, async (u) => {
      this.user = u;
      this.idToken = u ? await u.getIdToken() : null;
    });
  }

  /**
   * כניסה באמצעות אימייל וסיסמה.
   * @param email אימייל
   * @param password סיסמה
   */
  login(email: string, password: string) {
    return signInWithEmailAndPassword(this.auth, email, password);
  }

  /**
   * מסנכרן את המשתמש המחובר לשרת (יוצר/מעדכן רשומת משתמש ב־backend).
   * @param baseUrl כתובת בסיס ל־API (אופציונלי, ברירת מחדל מקובץ הריצה)
   */
  async syncMe(baseUrl?: string) {
    if (!this.user) return;
    const token = await this.user.getIdToken();
    const apiBase = baseUrl ?? this.cfg.apiBaseUrl;
    await fetch(`${apiBase}/api/users/me`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'ngrok-skip-browser-warning': 'true',
      },
    });
  }

  /**
   * יציאה מהחשבון (signOut) ב־Firebase.
   */
  logout() {
    return signOut(this.auth);
  }
}
