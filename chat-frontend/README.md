# Chat Frontend (Angular)

A realtime 1-to-1 chat frontend built with Angular. It authenticates users with Firebase Authentication, calls a Spring Boot backend over HTTPS, and connects to a WebSocket for live messages. The app is designed to be deployed to Firebase Hosting while the backend runs locally and is exposed over HTTPS using ngrok.

## Features

- Firebase Authentication (email/password or Google)
- Auth state-aware UI (login screen → chat screen)
- Recent conversations list and per-conversation history
- Realtime messaging over WebSocket
- Client-side message filtering and per-conversation unread badges
- Self-messaging prevention (both UI and backend validation)
- Runtime backend configuration via `public/runtime-config.json` (no rebuilds needed)
- ngrok interstitial bypass and CORS-friendly headers automatically added

## Tech Stack

- Angular 20 (standalone components, signals)
- Firebase Web SDK (Auth)
- REST + WebSocket to Spring Boot backend
- Firebase Hosting for static deployment

## App Structure (high level)

- `src/app/pages/login` — login screen
- `src/app/pages/chat` — chat screen (recent list, history, composer)
- `src/app/services/auth.service.ts` — Firebase Auth integration and backend user sync
- `src/app/services/chat.service.ts` — REST calls (history, recent, resolve) and WebSocket
- `src/app/services/config.service.ts` — runtime config loader (apiBaseUrl, wsUrl)
- `src/app/services/ngrok-bypass.interceptor.ts` — adds `ngrok-skip-browser-warning` header to API calls
- `src/app/models` — TypeScript interfaces for messages and conversations

## Prerequisites

- Node.js LTS and npm
- Angular CLI: `npm i -g @angular/cli`
- Firebase project with Authentication and Hosting enabled
  - In Firebase Console → Authentication → Sign-in method: enable Email/Password (and/or Google)
  - In Firebase Console → Hosting: set up a Hosting site
  - In Firebase Console → Authentication → Settings → Authorized domains: ensure your Hosting domains (`<project-id>.web.app`, `<project-id>.firebaseapp.com`) are present

## Firebase Web Config

The Firebase web config is read from `src/environments/environment.ts` and `environment.prod.ts`. Make sure the values match your Firebase project:

```ts
export const environment = {
  production: false,
  firebase: {
    apiKey: '<YOUR_API_KEY>',
    authDomain: '<YOUR_PROJECT>.firebaseapp.com',
    projectId: '<YOUR_PROJECT>',
  },
  // runtime backend config is injected at runtime; these are only for local defaults
  backendBaseUrl: 'http://localhost:8080',
  wsBaseUrl: 'ws://localhost:8080',
};
```

## Runtime Backend Configuration (no rebuilds)

The app loads `public/runtime-config.json` on startup to discover the backend URLs. Update this file whenever your backend URL changes (e.g., new ngrok URL):

```json
{
  "apiBaseUrl": "https://<your-ngrok-domain>",
  "wsUrl": "wss://<your-ngrok-domain>"
}
```

For local development without ngrok:

```json
{
  "apiBaseUrl": "http://localhost:8080",
  "wsUrl": "ws://localhost:8080"
}
```

This file is fetched with `Cache-Control: no-store` and also served with no-store headers on Firebase Hosting.

## Development (Frontend locally)

1) Install dependencies:

```bash
npm install
```

2) Start the Angular dev server:

```bash
ng serve
```

3) Open `http://localhost:4200/`.

4) Backend options:
- If your backend runs locally (http://localhost:8080), keep `public/runtime-config.json` pointing to `http://` and `ws://`.
- If your backend is exposed via ngrok, update `public/runtime-config.json` to `https://` and `wss://` for the ngrok domain.

## Deploy to Firebase Hosting (Backend via ngrok)

1) Run your backend locally on port 8080.
2) Start an HTTPS tunnel:

```bash
ngrok http 8080
```

Copy the HTTPS URL, e.g., `https://abcd-1234.ngrok-free.app`.

3) Update `public/runtime-config.json`:

```json
{
  "apiBaseUrl": "https://abcd-1234.ngrok-free.app",
  "wsUrl": "wss://abcd-1234.ngrok-free.app"
}
```

4) Login and deploy to Firebase Hosting:

```bash
npm i -g firebase-tools
firebase login
npm run build
firebase deploy --only hosting  # or: npm run deploy
```

Open your Hosting URL (e.g., `https://<project-id>.web.app`).

## How the Frontend Works

- Auth flow:
  - Login with Firebase Auth (email/password)
  - After login, the app calls `PUT /api/users/me` with the Firebase ID token to upsert the user in the backend directory
- REST:
  - `GET /api/conversations/recent?limit=20` to load recent conversations
  - `GET /api/messages?withUser=<uid>&limit=<n>` to load message history
  - `GET /api/users/resolve?...` to resolve a user by email/uid
- WebSocket:
  - Connects to `wss://<backend>/ws/chat?token=<FIREBASE_ID_TOKEN>`
  - Sends `{ senderId, receiverId, message }` and receives the same schema
- UI logic:
  - Filters incoming messages to the active conversation only
  - Caches messages per conversation in-memory
  - Shows unread badges per conversation
  - Prevents self-messaging in the UI

## ngrok Interstitial Warning (ERR_NGROK_6024)

The app automatically sends `ngrok-skip-browser-warning: true` on all API requests (via an HTTP interceptor) and on fetch calls. This bypasses the ngrok warning page that would otherwise return HTML instead of JSON.

WebSocket uses the direct `wss://` URL and is unaffected by this header; ensure you use `wss://` (TLS).

## Two-Device Demonstration

- Open two browser sessions (e.g., regular window and private/incognito)
- Login with two different Firebase users
- Start a conversation and verify:
  - Messages appear in realtime for both sides
  - Unrelated messages do not leak into the open conversation
  - Unread badge increments on other conversations

## Troubleshooting

- Mixed content error: ensure Hosting is HTTPS and your backend URL is `https://` and `wss://` when using ngrok
- ngrok HTML instead of JSON: ensure the `ngrok-skip-browser-warning` header is present (already handled by the app)
- 400 `Cannot start conversation with yourself`: the frontend shows a clear error and clears the email input; verify you are not resolving your own email/uid
- WebSocket won’t connect: confirm the `wsUrl` uses `wss://` and the backend WebSocket endpoint is reachable

## NPM Scripts

- `npm start` → `ng serve`
- `npm run build` → Production build
- `npm run deploy` → Build and deploy to Firebase Hosting

## Notes

- Only `public/runtime-config.json` needs updating when the ngrok URL changes; no rebuild required
- Firebase Hosting is configured to rewrite all routes to `index.html` (SPA) and to serve `runtime-config.json` with no-store headers