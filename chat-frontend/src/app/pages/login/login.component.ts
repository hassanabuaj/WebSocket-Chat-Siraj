import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="wrap">
      <h2>Login</h2>
      <div class="row">
        <input placeholder="Email" [(ngModel)]="email" />
      </div>
      <div class="row">
        <input type="password" placeholder="Password" [(ngModel)]="password" />
      </div>
      <div class="row">
        <button (click)="doLogin()">Login</button>
        <button (click)="doLogout()">Logout</button>
      </div>

      <div class="info" *ngIf="svc.user as u">
        <p><b>UID:</b> {{ u.uid }}</p>
        <p><b>Email:</b> {{ u.email }}</p>
      </div>

      <div class="info" *ngIf="!svc.user">
        <p>Not logged in.</p>
      </div>

      <p class="msg" *ngIf="msg()">{{ msg() }}</p>
    </div>
  `,
  styles: [
    `
      .wrap {
        max-width: 400px;
        margin: 60px auto;
        padding: 32px 28px 24px 28px;
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 4px 24px 0 rgba(0, 0, 0, 0.1);
        font-family: 'Segoe UI', Arial, sans-serif;
      }
      h2 {
        text-align: center;
        margin-bottom: 24px;
        color: #2d3748;
        font-weight: 600;
      }
      .row {
        margin: 14px 0;
        display: flex;
        gap: 10px;
      }
      input {
        flex: 1;
        padding: 12px 10px;
        border: 1px solid #2773cfff;
        border-radius: 6px;
        font-size: 1rem;
        background: #2f2d2dff;
        transition: border 0.2s;
      }
      input:focus {
        border: 1.5px solid #3182ce;
        outline: none;
        background: #2f2d2dff;
      }
      button {
        padding: 10px 18px;
        border: none;
        border-radius: 6px;
        background: #3182ce;
        color: #fff;
        font-weight: 500;
        font-size: 1rem;
        cursor: pointer;
        transition: background 0.2s, box-shadow 0.2s;
        box-shadow: 0 2px 8px 0 rgba(49, 130, 206, 0.08);
      }
      button:hover {
        background: #2563eb;
      }
      button:last-child {
        background: #e53e3e;
        margin-left: 8px;
      }
      button:last-child:hover {
        background: #b91c1c;
      }
      .info {
        background: #ffffffff;
        color: #222;
        padding: 14px 16px;
        border-radius: 8px;
        margin-top: 18px;
        font-size: 0.98rem;
        box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.04);
      }
      .msg {
        color: #e53e3e;
        background: #fff0f0;
        border: 1px solid #e53e3e33;
        border-radius: 6px;
        padding: 8px 12px;
        margin-top: 16px;
        text-align: center;
        font-weight: 500;
      }
    `,
  ],
})
export class LoginComponent {
  email = '';
  password = '';
  msg = signal<string>('');

  constructor(public svc: AuthService, private router: Router) {}

  /**
   * מבצע כניסה, מסנכרן את המשתמש בצד השרת ומנווט למסך הצ'אט.
   */
  async doLogin() {
    this.msg.set('');
    try {
      await this.svc.login(this.email, this.password);
      await this.svc.syncMe(); // ensure directory entry exists using runtime config
      this.router.navigate(['/chat']);
    } catch (e: any) {
      this.msg.set(e?.message ?? String(e));
    }
  }

  /**
   * מבצע יציאה מהחשבון.
   */
  async doLogout() {
    this.msg.set('');
    try {
      await this.svc.logout();
    } catch (e: any) {
      this.msg.set(e?.message ?? String(e));
    }
  }
}
