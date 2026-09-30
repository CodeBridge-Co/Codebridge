# CodeBridge Test Plan

## Overview
This document outlines the testing strategy for the CodeBridge system, covering unit, integration, and end-to-end tests.

## 1. Unit Tests

| Area | Target Classes | Owner | Description |
| :--- | :--- | :--- | :--- |
| **Android Sandbox** | `CodeExecutionEngine`, `JavaSandbox`, `TestCaseRunner` | Person A | Validate local code compilation and test case execution. |
| **Android Git** | `GitSimulationManager`, `JGitWrapper`, `MergeConflictGenerator` | Person A | Test Git command parsing and merge conflict simulation. |
| **Android Speech** | `SpeechRecorder`, `KeywordAnalyzer` | Person A | Test audio recording and keyword density calculation. |
| **Android UI** | ViewModels (`AuthViewModel`, `SandboxViewModel`) | Person B | Test state management and UI logic. |
| **Android Network** | `ApiService`, `ErrorParser`, `RetrofitClient` | Person D | Validate Retrofit endpoints, interceptors, and DTO parsing. |
| **Android Security** | `HashIdManager`, `SecurePreferences`, `AiRestrictionManager` | Person D | Test hash generation, encrypted storage, and AI limits. |
| **Android Sync** | `SyncQueueDao`, `SyncWorker` | Person D | Validate Room queue insertion, pending fetch, and retry logic. |
| **Backend Core** | `security.py`, `hashing.py`, `idempotency.py` | Person C | Test SHA-256 hashing, JWT generation, and idempotency keys. |
| **Backend CRUD** | `crud/*` | Person C | Test database operations for all entities. |
| **Backend Services** | `services/*` | Person C | Test business logic for sync, leaderboard, and AI assist. |

## 2. Integration Tests

| Area | Target | Owner | Description |
| :--- | :--- | :--- | :--- |
| **API End-to-End** | FastAPI Routers | Person C | Use `pytest` with an in-memory PostgreSQL or SQLite to test all endpoints. |
| **Network + Mock API** | `ApiService` + MockWebServer | Person D | Test Retrofit calls against mock responses matching the OpenAPI spec. |
| **Room + SyncQueue** | `SyncQueueDao` + `SyncWorker` | Person D | Test the full offline queue lifecycle using Room in-memory database. |

## 3. End-to-End (E2E) Tests

| Area | Target | Owner | Description |
| :--- | :--- | :--- | :--- |
| **Offline Sync Flow** | Android App + Backend | Person D + Person C | Simulate offline session creation, go online, verify sync to PostgreSQL. |
| **Auth Flow** | Android App + Backend | Person B + Person C | Register new user, login, verify JWT and session persistence. |
| **AI Assist Limit** | Android App + Backend | Person B + Person C | Verify local 3-use limit and backend enforcement on 4th attempt. |

## 4. Test Ownership Summary

- **Person A:** Sandbox, Git, Speech unit tests.
- **Person B:** UI, ViewModel unit tests.
- **Person C:** Backend unit, integration, and API tests.
- **Person D:** Network, Security, Sync unit and integration tests. E2E test coordination.

## 5. Tools

- **Android:** JUnit, Mockito, Room In-Memory Database, MockWebServer, Espresso, UI Automator.
- **Backend:** pytest, httpx, SQLAlchemy async test fixtures.
- **CI/CD:** GitHub Actions.
