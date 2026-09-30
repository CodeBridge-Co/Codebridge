# CodeBridge System Architecture

## High-Level Technical Design

The system is structured around a multi-tier model designed to support offline-first mobile execution, secure backend data persistence, and specialized third-party AI integrations.

### System Tiers

1. **Presentation Tier (Mobile Client):** Native Android application handling UI rendering, local code compilation, offline data caching, audio recording, and Git version control simulation.
2. **Services & AI Integration Tier:** External cloud AI providers, including speech-to-text (Whisper API) and generative AI hint assistants (Claude/GPT-4o).
3. **Application Tier (Backend Services):** Python and FastAPI REST API gateway processing telemetry syncs, calculating keyword metrics, managing authentication, and enforcing AI dependency thresholds.
4. **Data Tier (Centralized Database):** PostgreSQL database storing master problem banks, anonymized student performance metrics, and company event postings.

### Core Technology Stack

- **Mobile Frontend:** Android (Java), Room Persistence Library (SQLite)
- **Local Sandboxing:** Embedded runtime environment, JGit integration for merge conflict tasks
- **Backend API:** Python FastAPI, SQLAlchemy ORM, Uvicorn ASGI server
- **Central Database:** PostgreSQL
- **AI Models:** OpenAI Whisper (Speech-to-Text), Anthropic Claude / OpenAI GPT-4o (Generative AI)
- **Networking:** Retrofit 2, OkHttp 3

### Operational Data Flows

1. **Offline Mode and Synchronization:**
   When internet connection is lost, the app switches to offline mode. Code compilation, test execution, and telemetry logging occur on-device. Upon connectivity restoration, Retrofit 2 syncs locally cached Room SQLite payloads to PostgreSQL.

2. **Multi-Modal AI & Telemetry:**
   Behavioral data (execution speed, test correctness, Git errors) is logged locally. Spoken explanations in Stage 3 are uploaded to Whisper API for transcription. FastAPI parses the transcript using keyword matching to calculate `speech_keyword_density`, stored alongside the cryptographic hash ID.

3. **Employer and Student Interaction:**
   Students browse company hackathons and problems. Employers manage events/problems via CRUD operations, view anonymized leaderboards, and send in-app contact requests.

### Key Infrastructure Decisions

- **Offline-First Resilience:** Designed for network drops and high mobile data costs.
- **Privacy and Anonymization:** Cryptographic hash IDs assigned upon registration. No real names stored with telemetry.
- **Controlled AI Dependency:** Strict three-use limit per challenge attempt, enforced locally and validated on the backend.

### Architecture Diagram

![Architecture Diagram](path/to/your/diagram.png)
*(Note: Insert the architecture diagram from the brief here)*
