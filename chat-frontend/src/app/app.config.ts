import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ConfigService } from './services/config.service';
import { ngrokBypassInterceptor } from './services/ngrok-bypass.interceptor';

/**
 * קונפיגורציית האפליקציה: ניתוב, HttpClient ומאתחל טעינת קובץ הקונפיגורציה בזמן עלייה.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([ngrokBypassInterceptor])),
    {
      provide: APP_INITIALIZER,
      multi: true,
      deps: [ConfigService],
      useFactory: (cfg: ConfigService) => () => cfg.load(),
    },
  ],
};
