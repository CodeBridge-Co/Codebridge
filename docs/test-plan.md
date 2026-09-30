# CodeBridge Test Plan

## Overview
This document outlines the testing strategy for the CodeBridge system, covering unit, integration, and end-to-end tests.

## 1. Unit Tests

| Area | Target Classes | Owner | Description |
| :--- | :--- | :--- | :--- |
| **Android Sandbox** | `CodeExecutionEngine`, `JavaSandbox`, `TestCaseRunner` | thatomoloto | Validate local code compilation and test case execution. |
| **Android Git** | `GitSimulationManager`, `JGitWrapper`, `MergeConflictGenerator` | thatomoloto | Test Git command parsing and merge conflict simulation. |
| **Android Speech** | `SpeechRecorder`, `KeywordAnalyzer` | thatomoloto | Test audio recording and keyword density calculation. |
| **Android UI** | ViewModels (`AuthViewModel`, `SandboxViewModel`) | thatomoloto | Test state management and UI logic. |
| **Android Network** | `ApiService`, `ErrorParser`, `RetrofitClient` | Lebo624 | Validate Retrofit endpoints, interceptors, and DTO parsing. |
| **Android Security** | `HashIdManager`, `SecurePreferences`, `AiRestrictionManager` | Lebo624 | Test hash generation, encrypted storage, and AI limits. |
| **Android Sync** | `SyncQueueDao`, `SyncWorker` | Lebo624 | Validate Room queue insertion, pending fetch, and retry logic. |
| **Backend Core** | `security.py`, `hashing.py`, `idempotency.py` | BonoloNkosi24 | Test SHA-256 hashing, JWT generation, and idempotency keys. |
| **Backend CRUD** | `crud/*` | BonoloNkosi24 | Test database operations for all entities. |
| **Backend Services** | `services/*` | BonoloNkosi24 | Test business logic for sync, leaderboard, and AI assist. |

## 2. Integration Tests

| Area | Target | Owner | Description |
| :--- | :--- | :--- | :--- |
| **API End-to-End** | FastAPI Routers | BonoloNkosi24 | Use `pytest` with an in-memory PostgreSQL or SQLite to test all endpoints. |
| **Network + Mock API** | `ApiService` + MockWebServer | Lebo624 | Test Retrofit calls against mock responses matching the OpenAPI spec. |
| **Room + SyncQueue** | `SyncQueueDao` + `SyncWorker` | Lebo624 | Test the full offline queue lifecycle using Room in-memory database. |

## 3. End-to-End (E2E) Tests

| Area | Target | Owner | Description |
| :--- | :--- | :--- | :--- |
| **Offline Sync Flow** | Android App + Backend | Lebo624 + BonoloNkosi24 | Simulate offline session creation, go online, verify sync to PostgreSQL. |
| **Auth Flow** | Android App + Backend | thatomoloto + BonoloNkosi24 | Register new user, login, verify JWT and session persistence. |
| **AI Assist Limit** | Android App + Backend | thatomoloto + BonoloNkosi24 | Verify local 3-use limit and backend enforcement on 4th attempt. |

## 4. Test Ownership Summary

- **thatomoloto:** Sandbox, Git, Speech unit tests, UI, ViewModel unit tests.
- **BonoloNkosi24:** Backend unit, integration, and API tests.
- **Lebo624:** Network, Security, Sync unit and integration tests. E2E test coordination.

## 5. Tools

- **Android:** JUnit, Mockito, Room In-Memory Database, MockWebServer, Espresso, UI Automator.
- **Backend:** pytest, httpx, SQLAlchemy async test fixtures.
- **CI/CD:** GitHub Actions.
