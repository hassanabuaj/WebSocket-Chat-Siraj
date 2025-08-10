# Chat App Backend - Real-time WebSocket Chat with Firebase Auth

A Spring Boot backend for a real-time chat application featuring WebSocket communication, Firebase Authentication, MongoDB persistence, and Firebase Cloud Function integration.

## 🏗️ Architecture Overview

### Core Technologies
- **Spring Boot 3** - Main application framework
- **Spring WebSocket** - Real-time bidirectional communication
- **Spring Security** - Authentication and authorization
- **Spring Data MongoDB** - Database operations
- **Firebase Admin SDK** - User authentication and token verification
- **Maven** - Dependency management and build tool

### Key Features
- ✅ **Real-time messaging** via WebSocket with proper session management
- ✅ **Firebase Authentication** for secure user verification
- ✅ **MongoDB persistence** for message storage and user directory
- ✅ **Self-messaging prevention** at both WebSocket and user lookup levels
- ✅ **Firebase Function integration** for push notification simulation
- ✅ **CORS support** for hosted frontend integration
- ✅ **Clean MongoDB documents** (no `_class` field pollution)

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **MongoDB** running locally or accessible via connection string
- **Firebase Project** with service account credentials
- **Maven** (or use included wrapper `./mvnw`)

### 1. Clone and Setup
```bash
git clone <your-repository>
cd chat-app
```

### 2. Firebase Configuration
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Enable **Authentication** (Email/Password or Google Sign-In)
3. Generate a **service account key**:
   - Go to Project Settings → Service Accounts
   - Generate new private key
   - Save as `firebase-service-account.json` in project root

### 3. MongoDB Setup
Ensure MongoDB is running locally:
```bash
# Default connection: mongodb://localhost:27017/chatapp
mongod
```

### 4. Environment Configuration
Set environment variables (optional, defaults provided):
```bash
# Windows PowerShell
$env:FIREBASE_CREDENTIALS_PATH="firebase-service-account.json"
$env:FIREBASE_FUNCTION_NOTIFY_URL="http://localhost:5001/your-project-id/us-central1/notifyMessage"

# Linux/macOS
export FIREBASE_CREDENTIALS_PATH="firebase-service-account.json"
export FIREBASE_FUNCTION_NOTIFY_URL="http://localhost:5001/your-project-id/us-central1/notifyMessage"
```

### 5. Firebase Functions Setup (Optional but Recommended)
To receive notification logs when messages are sent:

```bash
# Navigate to functions directory
cd functions

# Install dependencies
npm install

# Start Firebase Functions emulator
firebase emulators:start --only functions
```

The Functions emulator will start on **http://localhost:5001**

### 6. Run the Application
```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using installed Maven
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

## 📡 API Endpoints

### Authentication
All REST endpoints require Firebase ID token in Authorization header:
```
Authorization: Bearer <firebase-id-token>
```

### REST Endpoints

#### User Management
- **PUT /api/users/me** - Sync current user to database
  - Headers: `Authorization: Bearer <token>`
  - Response: User details or error

- **GET /api/users/resolve** - Find user by email or UID
  - Query: `?email=user@example.com` OR `?uid=firebase-uid`
  - Headers: `Authorization: Bearer <token>`
  - Response: User details or 404/400 for self-lookup

#### Messages
- **GET /api/messages** - Get conversation history
  - Query: `?withUser=<uid>&limit=50`
  - Headers: `Authorization: Bearer <token>`
  - Response: Array of messages sorted by timestamp

#### Conversations
- **GET /api/conversations/recent** - Get recent conversation list
  - Query: `?limit=20`
  - Headers: `Authorization: Bearer <token>`
  - Response: Array of conversation summaries

### WebSocket Endpoint

#### Connection
```
ws://localhost:8080/ws/chat?token=<firebase-id-token>
```

#### Message Format
Send messages as JSON:
```json
{
  "senderId": "firebase-uid-sender",
  "receiverId": "firebase-uid-recipient", 
  "message": "Hello there!"
}
```

#### Error Responses
- `{"error":"senderId mismatch"}` - Sender doesn't match authenticated user
- `{"error":"cannot send message to yourself"}` - Self-messaging attempt
- Connection closed with `Unauthenticated` - Invalid or missing token

## 🗄️ Database Schema

### MongoDB Collections

#### `messages`
```json
{
  "_id": "ObjectId",
  "senderId": "string",
  "receiverId": "string", 
  "timestamp": "ISODate",
  "message": "string"
}
```

#### `users`
```json
{
  "_id": "firebase-uid",
  "email": "string",
  "displayName": "string",
  "updatedAt": "ISODate"
}
```

## 🔧 Configuration

### application.yml
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/chatapp
      database: chatapp

app:
  firebase:
    serviceAccount: ${FIREBASE_CREDENTIALS_PATH:firebase-service-account.json}
  functions:
    notifyUrl: ${FIREBASE_FUNCTION_NOTIFY_URL:http://localhost:5001/chat-app-hasan/us-central1/notifyMessage}
  cors:
    allowedOrigins: []  # Exact origins (optional)
    allowedOriginPatterns:
      - "https://*.web.app"
      - "https://*.firebaseapp.com" 
      - "http://localhost:4200"
```

### CORS Configuration
The app supports both exact origins and pattern matching for frontend integration:
- **Firebase Hosting**: `https://your-app.web.app`
- **Local Development**: `http://localhost:4200`
- **Custom domains**: Add to `allowedOrigins` or use patterns

## 🔒 Security Features

### Firebase Authentication
- **Token Validation**: Every request/connection validates Firebase ID tokens
- **User Principal**: Authenticated user UID is available in all endpoints
- **Session Management**: WebSocket sessions tied to authenticated UIDs

### Self-Messaging Prevention  
- **WebSocket Level**: Rejects messages where senderId equals receiverId
- **User Lookup Level**: Prevents resolving your own email/UID for conversations
- **Frontend Integration**: Returns appropriate error messages for UI handling

### Input Validation
- **Sender Verification**: WebSocket messages must come from authenticated user
- **Parameter Validation**: Required fields validated in REST endpoints
- **MongoDB Injection**: Spring Data provides protection against injection attacks

## 🌐 Integration with Frontend

### For Local Development + Hosted Frontend
1. **Start backend locally** on port 8080
2. **Use ngrok** to expose via HTTPS: `ngrok http 8080`
3. **Add ngrok-skip-browser-warning header** to all frontend requests:
   ```javascript
   headers: {
     'Authorization': `Bearer ${token}`,
     'ngrok-skip-browser-warning': 'true'
   }
   ```
4. **WebSocket connection**:
   ```javascript
   const ws = new WebSocket(`wss://your-ngrok-url/ws/chat?token=${token}&ngrok-skip-browser-warning=true`);
   ```

### Message Flow
1. **User Authentication**: Frontend authenticates with Firebase
2. **User Sync**: Frontend calls `PUT /api/users/me` to sync user to database  
3. **WebSocket Connection**: Frontend connects with Firebase ID token
4. **Real-time Messaging**: Messages sent via WebSocket, received by all participants
5. **History Loading**: Frontend loads conversation history via REST API
6. **Firebase Function**: Backend triggers notification function after each message

## 🐛 Troubleshooting

### Common Issues

#### "Unauthenticated" WebSocket connection
- Verify Firebase token is valid and not expired
- Check token is passed as query parameter: `?token=<token>`
- Ensure Firebase service account file is accessible

#### MongoDB Connection Failed
- Verify MongoDB is running: `mongod`
- Check connection string in `application.yml`
- Ensure database permissions are correct

#### CORS Errors
- Add your frontend domain to `allowedOriginPatterns`
- For ngrok, ensure `ngrok-skip-browser-warning` header is included
- Verify WebSocket connection uses correct origin patterns

#### Firebase Function Not Triggered
- **Start Functions emulator**: `firebase emulators:start --only functions` (from `/functions` directory)
- Check `FIREBASE_FUNCTION_NOTIFY_URL` environment variable points to correct emulator URL
- Verify Firebase Functions emulator is running on http://localhost:5001
- Check backend logs for HTTP request errors (non-critical)
- View function logs in Firebase emulator UI at http://localhost:4000

### Debug Logging
Add to `application.yml` for detailed logging:
```yaml
logging:
  level:
    com.assignmenthasan.chatapp: DEBUG
    org.springframework.web.socket: DEBUG
    org.springframework.security: DEBUG
```

## 📝 Development Notes

### Code Structure
- **Controllers**: REST endpoint handlers
- **WebSocket**: Real-time message handling and session management  
- **Services**: Business logic and external integrations
- **Models**: Database entities (MongoDB documents)
- **DTOs**: Data transfer objects for API communication
- **Config**: Application configuration and security setup

### Key Design Decisions
- **Stateless REST**: No server-side sessions, Firebase tokens per request
- **Single WebSocket Connection**: One connection per user, message routing via UID
- **Conversation Isolation**: Frontend filters messages by conversation context
- **Clean MongoDB**: Custom converter removes `_class` field pollution
- **Firebase Integration**: Admin SDK for token verification, Functions for notifications

### Performance Considerations
- **Concurrent Session Map**: Thread-safe storage for active WebSocket sessions
- **Database Indexing**: Email field indexed for fast user lookups
- **Message Limiting**: REST endpoints support pagination via `limit` parameter
- **Connection Cleanup**: Automatic removal of closed WebSocket sessions

## 🚀 Deployment

### Local Development
Perfect for development with ngrok tunneling to hosted frontend.

### Production Considerations
- **Environment Variables**: Use proper secrets management
- **MongoDB Atlas**: Use managed MongoDB for production
- **Firebase Production**: Use production Firebase project
- **HTTPS/WSS**: Ensure all connections use secure protocols
- **CORS**: Restrict to production domains only
- **Monitoring**: Add proper logging and metrics collection

---

The backend is ready for integration with Angular/Flutter frontend and Firebase Hosting deployment.
