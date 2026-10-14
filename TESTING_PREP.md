# CodeBridge — Testing Preparation Plan

**Status:** App works with mock data. Demo-ready.
**Goal:** Switch to real backend + complete proper testing before submission.

---

## Current State

| Component | Status |
|---|---|
| Android UI (Kotlin) | Working with mock data |
| Login flow | Wired to ApiService (mock) |
| Problems list | Loads from mock |
| Sandbox Run | Executes JS against mock test cases |
| Backend (FastAPI + Postgres) | Running, endpoints verified |
| Docker stack | Builds and runs |
| CI pipeline | 4 jobs green |
| Git, Speech, Events, Results | Mocked, Week 3 work |

---

## Testing Goal

Move from demo-verified (mock data, happy path) to properly-tested (real backend, edge cases, offline sync, error handling).

---

## Test Areas

| # | Area | Owner | Priority |
|---|---|---|---|
| 1 | Backend endpoint tests (pytest) | Person C | High |
| 2 | Real backend integration (Android → FastAPI → Postgres) | Person D | High |
| 3 | Offline → online sync flow | Person D | High |
| 4 | Room database + SQLCipher | Person A | High |
| 5 | Sandbox execution engine | Person A | High |
| 6 | Login/register edge cases | Person B + A | Medium |
| 7 | Error handling (network, 4xx, 5xx) | Person D | Medium |
| 8 | Security (hash ID, AI limit) | Person A + D | Medium |
| 9 | End-to-end demo flow | Everyone | High |
| 10 | Git + Speech engines | Person A | Week 3 |

---

## Person A — Android Core

1. Confirm `local.properties` is gitignored and untracked
2. Verify Room entities match DTOs via mappers (compile + unit test)
3. Add unit tests for `AppDatabase` (in-memory Room + SQLCipher)
4. Add unit tests for `CodeExecutionEngine` — pass, fail, syntax error, timeout, empty suite
5. Write `TelemetrySink` implementation that writes to `TelemetryLogEntity`
6. Wire `TlsInterceptor` into `OkHttpClientProvider`
7. Document Week 3 scope: `core/git`, `core/speech`

**Deliverable:** All Android unit tests pass in CI.

---

## Person B — UI (Kotlin)

1. Push current branch to origin so CI runs against your code
2. Add input validation tests for login (student number format, institution required)
3. Wire `EventsFragment` to `apiService.getEvents()`
4. Wire `ResultsFragment` to real scoring data from sandbox callback
5. Add loading + error states to `ProblemsFragment` and `LoginFragment`
6. Remove `ProblemRepository.kt` hardcoded list — read from `apiService.getProblems()`
7. Add "Week 3" labels to Git and Speech screens

**Deliverable:** All UI paths call real backend or are explicitly labelled as mocked.

---

## Person C — Backend

1. Add pytest tests for every endpoint:
   - `test_health`, `test_root`
   - `test_register`, `test_login` (success + duplicate)
   - `test_get_problems`, `test_get_problem_by_id`, `test_get_problem_404`
   - `test_create_session`
   - `test_sync_idempotency` (same payload twice, verify no duplicates)
   - `test_leaderboard`, `test_events`, `test_ai_assist_limit`, `test_speech_transcribe`
2. Add `Company` + `Event` models to `models.py`
3. Add `/sync` test verifying telemetry routes by `entity_type`
4. Add `/ai-assist` test verifying 3-use limit resets per session
5. Split `main.py` into `app/api/v1/routers/` — one file per resource
6. Add `app/api/v1/api.py` aggregator

**Deliverable:** `pytest -v` runs 15+ tests, all pass in CI.

---

## Person D — Network, Sync, Testing

1. **Real backend integration test** — flip `USE_MOCK_API = false`, run full flow on phone:
   - Register → verify row in Postgres
   - Login → verify token
   - Load problems → verify 3 problems with real test_cases_json
   - Submit session → verify row in `assessment_sessions`
   - Check leaderboard reflects updated XP
2. **Offline sync test**:
   - WiFi off → complete session
   - Verify `SyncQueueEntity` has PENDING item
   - WiFi on → wait for `SyncWorker`
   - Verify item becomes SYNCED
   - Verify data in Postgres
3. **Error handling tests** — mock 401, 404, 500, timeout; verify error message + no crash
4. **Contract tests** — Gson deserialization for `ProblemDto`, `SessionDto`, `TelemetryDto`, `LeaderboardDto`, `EventDto`
5. **Documentation** — final `HANDOVER.md` update with testing results

**Deliverable:** End-to-end integration report with screenshots/logs.

---

## Timeline

| Day | Milestone |
|---|---|
| Wednesday | Demo complete, TESTING_PREP.md distributed |
| Thursday | Person A/B/C task 1–2 done, Person D integration test started |
| Friday | Offline sync test passes, all unit tests added |
| Saturday | Full E2E — app → real backend → Postgres → sync |
| Sunday | Bug fixes, retest |
| Monday | Freeze, final report write-up |

---

## Report Contribution

Each person writes their section:

- **Person A:** Sandbox execution engine, Room entities + DAOs, SQLCipher setup, Git simulator
- **Person B:** UI/UX architecture, Kotlin-Java interop, screen flow, ViewModel patterns
- **Person C:** FastAPI architecture, SQLAlchemy models, sync idempotency, AI-limit enforcement
- **Person D:** Network layer, security, offline sync engine, CI/CD, testing infrastructure, integration results

Each section covers: what you built, why (design decisions), what worked, what's mocked, evidence.

---

## Definition of Done

1. All CI jobs green with expanded coverage
2. Real backend integration verified — app talks to FastAPI + Postgres, not mocks
3. Offline sync verified end-to-end
4. Every endpoint has at least one pytest
5. Every DTO has a deserialization test
6. Every error path shows a message, not a crash
7. `HANDOVER.md` refreshed

---

## Out of Scope

- Whisper API real integration
- Git simulator real execution
- Speech recording + keyword analysis
- Employer portal
- Alembic migrations
- TLS certificate pinning
- App-switch detection beyond `onPause`/`onResume`

---

## Rules

1. Test on the real backend, not just mocks.
2. One task at a time. Don't batch.
3. CI runs every push. If it fails, fix before pushing the next thing.
4. Log the evidence — screenshots, curl outputs, test reports.
5. Document deviations — say why, not just that it wasn't done.
