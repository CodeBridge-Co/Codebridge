# CodeBridge

### A Quantifiably Scalable Performance Assessment Application for Evaluating Technical Competency in Undergraduate IT Students

---

## Overview

CodeBridge is a native Android application paired with a FastAPI + PostgreSQL backend, designed to assess and quantify the technical competency of undergraduate IT students through realistic, hands-on engineering scenarios.

The platform replaces traditional pen-and-paper assessments with a structured four-stage challenge flow:

1. Code Sandbox - solve algorithmic problems with automated test case execution
2. Git Conflict Resolution - resolve simulated merge conflicts using a lightweight command simulator
3. Verbal Explanation - record spoken reasoning that is transcribed and analysed for technical keyword density
4. Results and Reward - per-session scoring with XP, streaks, badges, and cohort leaderboards

The system is built offline-first to accommodate unreliable campus networks. Sessions completed without connectivity are queued locally and synced idempotently to the central PostgreSQL database when the device reconnects.

---

## Problem Statement

Undergraduate IT assessment typically relies on written exams and take-home projects. Both fail to capture:

- Real-time problem-solving ability - how a student reasons under time pressure
- Tool proficiency - Git, debugging, iterative development
- Communication - the ability to explain technical decisions verbally
- Consistency - sustained practice versus last-minute cramming
- Scalability - lecturer bandwidth limits cohort size and assessment frequency

CodeBridge addresses each by instrumenting the development workflow itself: measuring execution speed, correctness, Git error frequency, and verbal keyword density per session, all under anonymised hash IDs.

---

## Key Features

| Feature | Description |
|---|---|
| Offline-first challenge flow | Sessions run fully on-device, queued in Room, synced when online |
| On-device code sandbox | Runs JavaScript via Rhino against per-problem test cases |
| Automated test case scoring | Pass/fail per case, execution time, stdout capture |
| Git conflict simulator | Lightweight command parser simulating add, commit, merge |
| Speech-to-text pipeline | Records explanations; transcript keyword density feeds analytics |
| Anonymised student identity | Salted SHA-256 hash IDs - no real names stored with telemetry |
| Controlled AI assistance | 3-use limit per session enforced locally and on the backend |
| Gamification | XP, streak count, badges per student |
| Cohort leaderboard | Anonymised rankings by XP |
| Employer events portal | Company-posted hackathons and networking events |
| CI/CD | GitHub Actions runs Android, backend, Docker, and integration tests |
| Encrypted local storage | Room database with SQLCipher + Android Keystore |

---

## System Architecture

CodeBridge follows a four-tier architecture.

Tier 1: Client / Actor Layer
- Student (Android)
- Employer / Company (API)

Tier 2: Presentation Tier - Android Mobile Client
- Native UI (Kotlin + XML)
- Code Sandbox (Rhino JS engine, test-case runner)
- Git Simulator (command parser + merge conflict generator)
- Audio Capture (MediaRecorder + SpeechRecognizer)
- Retrofit 2 + OkHttp 3 (network layer)
- Room + SQLCipher (local encrypted persistence)

Tier 3: Application Tier - Backend Services
- FastAPI (async ASGI)
- SQLAlchemy 2.0 (async ORM)
- Pydantic (request/response validation)
- Uvicorn (ASGI server)

Tier 4: Data Tier - Centralised PostgreSQL Database
- students
- problems
- assessment_sessions
- telemetry_logs
- reward_progress
- sync_logs
- companies
- events

### Operational Data Flows

Offline to Online Sync
1. Student completes a session offline - local Room DB stores session + telemetry
2. SyncWorker (WorkManager) detects connectivity
3. Batches queued items into a SyncPayload keyed on client-generated IDs
4. POST /sync upserts idempotently to PostgreSQL
5. Local queue marks items SYNCED

Challenge Session Flow
1. Student picks a problem - GET /problems/{id} returns test cases
2. Sandbox executes user code against each case, produces ExecutionResult
3. Telemetry captured (execution time, correctness, Git errors, AI assists)
4. POST /sessions stores the session record
5. POST /sync (or offline queue) pushes telemetry

Anonymisation
- Student number hashed with salted SHA-256 at the client (Android Keystore-stored salt)
- Backend stores only the hash, never the raw number
- All telemetry, rewards, and leaderboard entries reference the hash ID

---

## Technology Stack

| Layer | Technology |
|---|---|
| Android UI | Kotlin + XML layouts, Jetpack Navigation |
| Android Core | Java - Room, Retrofit, OkHttp, WorkManager |
| Sandbox | Mozilla Rhino (JavaScript, interpreted) |
| Local Storage | Room + SQLCipher, EncryptedSharedPreferences |
| Network | Retrofit 2, OkHttp 3, Gson |
| Backend | Python 3.11, FastAPI, SQLAlchemy 2.0 (async), Pydantic v2 |
| Database | PostgreSQL 15 |
| Server | Uvicorn (ASGI) |
| Infra | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Testing | JUnit 4, MockWebServer, pytest, FastAPI TestClient |

---

## Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Android Studio | latest |
| JDK | 17 |
| Android SDK | API 34 |
| Python | 3.11+ |
| Docker Desktop | latest |
| Git | any |

### Backend Setup

Clone the repo:

    git clone https://github.com/CodeBridge-Co/Codebridge.git
    cd Codebridge

Start the full stack (Postgres + backend + pgAdmin):

    cd infra
    docker compose up -d

Wait for health check, then verify:

    curl http://localhost:8000/health
    # Returns: {"data":"OK","error":null,"status":"success"}

    curl http://localhost:8000/problems
    # Returns: [{"problem_id":1,"title":"Two Sum",...},...]

The backend auto-creates tables on startup and seeds 3 sample problems plus a demo student.

### Endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | / | Root status |
| GET | /health | Health check |
| POST | /auth/register | Create student |
| POST | /auth/login | Login, returns JWT |
| GET | /problems | List all problems |
| GET | /problems/{id} | Single problem |
| POST | /sessions | Create assessment session |
| POST | /sync | Batch offline sync (idempotent) |
| GET | /leaderboard | Cohort rankings |
| GET | /events | Company events |
| POST | /ai-assist | AI hint (3-use limit enforced) |
| POST | /speech/transcribe | Speech transcript + keyword density |

Full contract: docs/api-contracts/openapi.json

### Android Setup

    cd android-app

Create local.properties (untracked - machine-specific).

Windows:

    sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

Mac/Linux:

    sdk.dir=/Users/<you>/Library/Android/sdk

Build:

    ./gradlew assembleDebug

Install on connected device:

    ./gradlew installDebug

### Pointing the App at the Backend

In android-app/app/build.gradle:

    buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8000/\""          // emulator
    // buildConfigField "String", "BASE_URL", "\"http://192.168.x.x:8000/\""   // physical device
    buildConfigField "boolean", "USE_MOCK_API", "false"

- 10.0.2.2 is the emulator's alias for the host machine.
- For a physical device, use the laptop's LAN IP (ipconfig / ifconfig), and ensure both are on the same WiFi network.

Set USE_MOCK_API to true to develop UI without a running backend. The mock (MockApiInterceptor) returns schema-accurate responses for all endpoints.

---

## Testing

### Android

    cd android-app
    ./gradlew testDebugUnitTest       # unit tests
    ./gradlew connectedAndroidTest    # instrumentation (needs emulator/device)

### Backend

    cd backend
    pip install -r requirements.txt
    pytest -v

Tests run against SQLite for speed - no Postgres required.

### Full Stack Integration

    cd infra
    docker compose up -d

Then in Android: set USE_MOCK_API=false, point BASE_URL at your laptop IP. Run the app, complete a session, then check:

    docker exec -it codebridge_postgres psql -U codebridge_user -d codebridge_db -c "SELECT * FROM assessment_sessions;"

### CI

Every push to main or develop runs:

| Job | Purpose |
|---|---|
| Android Unit Tests | Compiles + runs JUnit tests |
| Backend Unit Tests | Runs pytest |
| Build Docker Image | Validates Dockerfile |
| Integration Test (Postgres + Backend) | Spins up full stack, hits endpoints |

---

## Engineering Trade-offs

Explicit design decisions made for the MVP, with rationale.

| Decision | Why | Post-MVP |
|---|---|---|
| Rhino JS sandbox instead of on-device Java compilation | Android has no javac; dynamic code loading is restricted on API 26+ | Evaluate GraalVM or remote execution |
| SQLite for backend tests instead of Postgres | CI speed, no external dependency | Keep - Postgres covered by integration job |
| create_all for schema instead of Alembic migrations | Faster iteration during MVP | Add Alembic for production migrations |
| In-memory AI-limit tracking on backend | Sufficient for MVP demo | Redis or persistent table |
| Mocked Whisper + GenAI | API key management, rate limits, and cost control out of scope | Real integration with key vaults |
| Simplified backend folder layout | Timeline | Split into routers / services / crud / utils |
| App-switch detection via onPause | System-wide detection requires restrictive permissions | Document as best-effort telemetry |
| Kotlin UI + Java core | Modern Android practice; interop is standard | Continue |

Full rationale in docs/architecture.md.

---

## Security and Privacy

- Anonymisation: Student numbers are hashed with SHA-256 + per-install salt before leaving the device. Salt is stored in Android Keystore. Backend never sees raw identifiers.
- Local storage: Room database encrypted with SQLCipher; AES-256 key generated in Android Keystore.
- Preferences: JWT tokens and salts stored in EncryptedSharedPreferences (AndroidX Security).
- Transport: TLS enforced; cleartext HTTP only permitted for local development hosts (10.0.2.2, localhost).
- AI dependency tracking: 3-use limit per session, enforced both client-side and server-side to prevent reinstall bypass.

---

## Team and Responsibilities

| Role | Owns |
|---|---|
| Android Core Engines | core/database, core/sandbox, core/git, core/speech, domain use cases |
| Android UI/UX | feature/*, ui/, layouts, ViewModels, navigation |
| Backend Lead | Entire backend/, FastAPI routes, SQLAlchemy models, migrations |
| Network, Security, Data, Infra | core/network, core/security, core/data, infra/, .github/, docs/, testing infra |

Detailed task ownership and integration notes: HANDOVER.md.

---

## Roadmap

### Completed (Weeks 1-3)

- Repository scaffolding and Gradle skeleton
- Android network layer (Retrofit, OkHttp, DTOs, mock)
- Security layer (HashIdManager, SQLCipher, SecurePreferences)
- Offline sync engine (SyncQueue, WorkManager, idempotent /sync)
- Backend (FastAPI + Postgres + SQLAlchemy async)
- Code sandbox (Rhino JS engine with test-case runner)
- Encrypted local Room database
- CI/CD pipeline (4 jobs, all green)

### In Progress (Weeks 4-5)

- Register screen
- Expanded test coverage (Android + backend)
- Git conflict simulator
- Speech recording + keyword analysis
- Error handling and loading states across all screens

### Post-MVP

- Real Whisper API integration
- Real Claude / GPT-4o integration for AI hints
- Employer web portal (currently Swagger UI)
- Full Alembic migration history
- Backend folder restructuring (routers / services / crud)
- TLS certificate pinning
- Redis-backed AI limit tracking
- Persistent offline audio queue for speech transcription

---

## License

Academic project. Not licensed for commercial use.

---

## Acknowledgements

- FastAPI, SQLAlchemy, Pydantic - backend stack
- Mozilla Rhino - on-device JavaScript engine
- Zetetic SQLCipher - encrypted SQLite
- AndroidX Security - encrypted preferences
- OkHttp MockWebServer - test infrastructure

---

Last updated: 2026-10-15
