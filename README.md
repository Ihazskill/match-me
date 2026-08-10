# MatchMe

MatchMe is a full-stack recommendation application for finding local connections through shared interests, hobbies, preferences, and personality. Users complete a profile, receive prioritized recommendations, request connections, and chat in real time after connecting.

## Technology

- Backend: Java 21, Spring Boot 4, Spring Security, JPA, WebSocket/STOMP
- Frontend: React 19 and TypeScript
- Database: PostgreSQL 15
- Authentication: bcrypt password hashes and JWT sessions

## Prerequisites

- Docker Desktop
- Java 21
- Node.js 20 or newer

The repository includes Maven Wrapper, so a separate Maven installation is not required.

## Setup

Start PostgreSQL from the repository root:

```powershell
docker compose up -d
```

Optionally set a stable JWT signing secret. Without one, Spring generates a new development secret at each backend startup, which invalidates existing sessions.

```powershell
$env:JWT_SECRET = "replace-with-a-random-secret-at-least-64-characters-long"
```

Start the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use `./mvnw spring-boot:run`.

In another terminal, start the frontend:

```powershell
cd frontend
npm install
npm start
```

Open http://localhost:3000. The API runs at http://localhost:8080.

## Usage

1. Register with a unique email and a password of at least eight characters.
2. Complete the identity profile and matching bio, including a city.
3. Review recommendations ordered by compatibility. Connect or permanently dismiss each candidate.
4. Accept or dismiss incoming requests from **Requests**.
5. Open an accepted connection and select **Message** to start or resume its single chat history.
6. Use **My Profile** to change profile fields, city, or profile picture.
7. Use **Logout** in the navigation from any authenticated page.

Chat messages, typing state, presence, unread counts, and conversation ordering update through WebSocket events without polling. Message history is loaded in pages of 30.

## Recommendation Rules

Candidates must have completed profiles, pass location checks, have no existing pending/accepted connection, and not have been dismissed. Scores combine interests, hobbies, music, food, lifestyle, personality, mutual sought interests, and location preference. Candidates below 20 compatibility points are excluded, results are sorted descending, and at most 10 IDs are returned.

Users may grant browser geolocation and choose a radius from 1 to 500 km. When both profiles have coordinates, recommendations use Haversine distance and the requesting user's radius. If precise location is not enabled, matching falls back to the same-city rule.

## Review Fixtures

Fixture loading is opt-in and idempotent. It creates 100 completed users with varied profiles. All fixture accounts use password `Review123!`; emails range from `reviewer1@matchme.test` to `reviewer100@matchme.test`.

```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=fixtures
```

To demonstrate a completely empty database, stop the applications and remove the PostgreSQL volume:

```powershell
docker compose down -v
docker compose up -d
```

Start the backend normally for no fixture users, create a few through the UI, or start it with the `fixtures` profile for 100 users.

## Tests

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm test -- --watchAll=false
npm run build
```

## Core API

- `GET /api/me`, `/api/me/profile`, `/api/me/bio`
- `GET /api/users/{id}`, `/api/users/{id}/profile`, `/api/users/{id}/bio`
- `GET /api/recommendations`
- `GET /api/connections` and `/api/connections/pending`
- `GET /api/chats`
- `GET /api/chats/{id}/messages?page=0&size=30`
- WebSocket/STOMP endpoint: `/ws`

Other-user profile endpoints return HTTP 404 for missing or unauthorized profiles. Authentication fields and email addresses are never included in profile, recommendation, connection, or chat responses.