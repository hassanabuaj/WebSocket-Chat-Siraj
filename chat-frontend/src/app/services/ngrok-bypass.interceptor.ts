import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ConfigService } from './config.service';

/**
 * Interceptor שמוסיף כותרת `ngrok-skip-browser-warning` לכל בקשות ה־HTTP
 * היוצאות לשרת ה־API המוגדר, כדי לעקוף את דף האזהרה של ngrok.
 */
export const ngrokBypassInterceptor: HttpInterceptorFn = (req, next) => {
  const cfg = inject(ConfigService);
  try {
    const api = new URL(cfg.apiBaseUrl);
    const reqUrl = new URL(req.url, window.location.origin);
    if (reqUrl.origin === api.origin) {
      const cloned = req.clone({
        setHeaders: { 'ngrok-skip-browser-warning': 'true' },
      });
      return next(cloned);
    }
  } catch {
    // ignore URL parsing errors and pass through
  }
  return next(req);
};


