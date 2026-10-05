# CodeBridge — Lebo624 Handover

**Author:** Lebo624 (Network, Security, Data, Infra, CI/CD, Testing, Docs)
**Status:** Complete — awaiting integration from Person A, B, C
**Last Updated:** 2026-10-05

---

## Summary

The infrastructure lead's (Lebo) deliverables are complete, committed, and validated by CI. The full stack (Android app, backend, Postgres, Docker) has been tested locally end-to-end. Everything is ready for integration once the other team members push their work.

**CI status:** All 4 jobs green ✅
- Android Unit Tests 🟢
- Backend Unit Tests🟢
- Build Docker Image🟢
- Integration Test (Postgres + Backend)🟢

---

## What Was Delivered

### Android — `core/network/`
- `ApiService.java` — all 11 endpoints matching OpenAPI spec
- `RetrofitClient.java` — singleton Retrofit instance
- `OkHttpClientProvider.java` — centralised OkHttp with interceptors
- `AuthInterceptor.java` — attaches JWT to requests
- `TlsInterceptor.java` — enforces HTTPS (bypassed for localhost)
- `MockApiInterceptor.java` — mock responses for all endpoints
- `NetworkMonitor.java` — LiveData<Boolean> online/offline state
- `TokenManager.java` — JWT storage
- DTOs: `ProblemDto`, `SessionDto`, `TelemetryDto`, `SyncPayload`, `SyncItem`, `RewardDto`, `EventDto`, `LeaderboardDto`, `SpeechResultDto`, `ApiResponse`, `ErrorResponse`

### Android — `core/security/`
- `HashIdManager.java` — salted SHA-256 student IDs via Android Keystore
- `SqlCipherManager.java` — Room encryption key provider
- `SecurePreferences.java` — EncryptedSharedPreferences wrapper
- `AiRestrictionManager.java` — 3-use AI limit (local)
- `AppSwitchDetector.java`, `FocusLossLogger.java`, `ClipboardMonitor.java` — telemetry detectors
- `PenaltyEngine.java`, `SessionLockManager.java` — penalty logic
- `IntegrityChecker.java` — root/emulator detection
- `TelemetrySink.java`, `LogcatTelemetrySink.java` — telemetry interface + default impl

### Android — `core/data/` + `core/domain/`
- `DataRepository.java` — single source of truth
- `LocalDataSource.java`, `RemoteDataSource.java`
- `SyncQueueEntity.java`, `SyncQueueDao.java` — offline queue
- `SyncWorker.java` — background sync via WorkManager
- `SyncOfflineDataUseCase.java` — sync orchestrator
- Mappers: `ProblemMapper`, `SessionMapper`, `TelemetryMapper`

### Backend — `backend/app/`
- `main.py` — all 10 endpoints against Postgres via SQLAlchemy ORM
- `models.py` — SQLAlchemy models matching ERD
- `schemas.py` — Pydantic models
- `core/config.py` — settings with safe defaults for CI
- `db/session.py`, `db/base.py`, `db/__init__.py`

### Backend — Root
- `backend/Dockerfile` — multi-layer build with healthcheck
- `backend/requirements.txt` — all dependencies
- `backend/pytest.ini` — pytest config
- `backend/tests/conftest.py` — test infrastructure
- `backend/tests/test_smoke.py` — placeholder test

### Infra — `infra/`
- `docker-compose.yml` — Postgres + backend + pgAdmin
- `env/backend.env` — backend environment variables
- `env/postgres.env` — Postgres credentials
- `postgres/init/` — schema init scripts

### Docs — `docs/`
- `api-contracts/openapi.json` — OpenAPI 3.0 spec (frozen contract)
- `api-contracts/postman-collection.json` — Postman collection
- `data-dictionary.md` — every entity + field from the ERD
- `architecture.md` — multi-tier architecture summary
- `test-plan.md` — test ownership and scope

### CI/CD — `.github/workflows/`
- `ci.yml` — 4 jobs: Android tests, backend tests, Docker build, integration test

---

## Integration Instructions

### For Android Core Lead 🟠

**What to check:**
1. Entity setter/field names match the mappers in `core/data/mapper/`. If they don't, tell Person D and the mappers get patched in one pass.
2. `AppDatabase` includes `SyncQueueEntity` and uses `SqlCipherManager.getSupportFactory(context)`.

**What to wire:**
1. `TlsInterceptor` — add to `OkHttpClientProvider` (one line, currently written but not added)
2. `TelemetrySink` — implement a Room-backed version that writes to `TelemetryLogEntity`
3. `SyncWorker.getSyncQueueDao()` — currently handled via `AppDatabase.getInstance(context).syncQueueDao()`. Verify it matches your implementation.

**What to test:**
- `./gradlew test` — Android unit tests
- Push, watch CI

### For UI/UX lead 🔵

**What to use:**
1. All API calls go through `RetrofitClient.getInstance().getApiService()`
2. Observe online/offline state via `NetworkMonitor.getIsOnline()` (LiveData<Boolean>)
3. Access data via `DataRepository.getInstance()`

**Mocking during development:**
- Set `USE_MOCK_API = true` in `app/build.gradle`
- `MockApiInterceptor` returns realistic responses for all endpoints
- Flip to `false` when integrating with the real backend

**What to wire:**
- Login screen → `apiService.login()`
- Problems list → `apiService.getProblems()`
- Sandbox → `apiService.getProblem(id)` + `apiService.createSession()`
- Leaderboard → `apiService.getLeaderboard()`
- Events → `apiService.getEvents()`
- Sync indicator → observe `NetworkMonitor.getIsOnline()`

### For Backend Lead 🟡

**What's already working:**
- `main.py` runs against Postgres
- All 10 endpoints respond with correct JSON
- Tables created automatically on startup (`Base.metadata.create_all`)
- Seed data inserted on startup (3 problems + demo student)
- `backend/tests/conftest.py` provides test fixtures

**What to build for the blueprint rubric:**
1. Split `main.py` into `app/api/v1/routers/*.py` (one file per resource)
2. Add `app/api/v1/api.py` (aggregator)
3. Extract business logic into `app/services/`
4. Extract DB queries into `app/crud/`
5. Add `app/utils/hashing.py` (salted SHA-256) and `app/utils/idempotency.py`
6. Optional stubs: `app/integrations/`, `app/workers/`
7. Skip Alembic — `create_all` handles the schema

**Contract rules:**
- Do NOT change field names in `docs/api-contracts/openapi.json` without team agreement
- `/sync` must use `json.dumps()` for payload storage (not `str()`)
- `/ai-assist` enforces 3-use limit server-side (already done)

---

## How to Run Locally

### Backend + Postgres

```powershell
cd C:\Users\lebok\Codebridge\infra
docker compose up -d
