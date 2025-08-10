# WebSocket Chat Application

A real-time 1-to-1 chat application built with **Spring Boot (Java)** for the backend and **Angular** for the frontend. Auth via **Firebase Authentication**, data in **MongoDB**, realtime via **WebSockets**. The frontend is deployed to **Firebase Hosting** and talks to the backend through an **ngrok** tunnel.

## Project Structure

```
root/
├─ chat-app/           # Backend (Spring Boot + MongoDB)
│  └─ BACKEND_README.md
├─ chat-frontend/      # Frontend (Angular)
│  └─ FRONTEND_README.md
└─ README.md           # This file
```

## Quick Start (Hosting Flow)

### 1) Backend (local)

1. Install prerequisites: **Java 17**, **Maven**, **MongoDB**.
2. Configure Firebase Admin credentials for the backend (see `chat-app/BACKEND_README.md`).
3. Run backend:
   ```bash
   cd chat-app
   ./mvnw spring-boot:run
   ```
4. In a new terminal, expose it with **ngrok**:
   ```bash
   ngrok http 8080
   ```
   Copy the HTTPS URL, e.g. `https://<id>.ngrok-free.app`.
5. (Optional for local functions testing) Start Firebase Functions emulator:
   ```bash
   firebase emulators:start --only functions
   ```

### 2) Frontend (build & deploy to Firebase Hosting)

1. Update runtime config **with the ngrok URLs**:
   - File: `chat-frontend/public/runtime-config.json`
   ```json
   {
     "apiBaseUrl": "https://<id>.ngrok-free.app",
     "wsUrl": "wss://<id>.ngrok-free.app"
   }
   ```
2. Build the Angular app:
   ```bash
   cd chat-frontend
   npm install
   ng build
   ```
3. Deploy to **Firebase Hosting**:
   ```bash
   firebase deploy --only hosting
   ```
4. Open the Hosting URL printed in the console (e.g., `https://<project>.web.app`).

> Note: Every time ngrok gives you a new URL, update `public/runtime-config.json` and redeploy hosting (`ng build` → `firebase deploy --only hosting`). No backend rebuild is required.

## Features

- Firebase Authentication (email/password or Google)
- Spring Boot REST + WebSocket
- MongoDB persistence
- Angular UI with chat history & realtime updates
- ngrok interstitial bypass handled by the frontend

## Need Details?

- Backend setup, endpoints, and security: `chat-app/BACKEND_README.md`
- Frontend structure, scripts, and troubleshooting: `chat-frontend/FRONTEND_README.md`


## Where I Used AI

I used AI tools as a resource throughout this project, mainly to help me understand concepts, troubleshoot issues, and speed up development—especially on the frontend (Angular) side. For the backend, I relied on AI to explain Spring Boot best practices, guide me through setting up authentication with Firebase etc...

AI was a helpful assistant for learning and problem-solving, but all the implementation, integration, and decision-making was done by me. This project was a valuable opportunity to deepen my understanding of both Angular


## Demo on YT
[![Demo] (https://youtu.be/HmW72o1xv34)]