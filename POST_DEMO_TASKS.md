# CodeBridge — Post-Demo Task Report

**Demo status:** Working with mock data
**Date:** 2026-10-14
**Next milestone:** Full integration + real testing

---

## Priority 0 — Wire the three mocked paths (Demo used mocks; these are real now)

### Task 0.1 — Login → real backend
**Owner:** Person B (UI) + Person A (integration)
**Files:** `feature/auth/LoginFragment.kt`, `core/security/HashIdManager.java`
**Steps:**
1. Change login fields to Student Number + Institution ID
2. Call `HashIdManager(context).generateHashId(studentNumber)`
3. `apiService.login(LoginRequest(hashId, institutionId))`
4. `TokenManager.saveToken(response.body().token)`
5. Navigate to problems on success
6. Delete `backend/HashIdManager.kt` (duplicate)

**Acceptance:** Login hits `/auth/login`, token persists across app restart.

---

### Task 0.2 — Problems list → real backend
**Owner:** Person B
**Files:** `feature/problems/ProblemsFragment.kt`, `feature/problems/ProblemRepository.kt`
**Steps:**
1. Delete the five hardcoded problems from `ProblemRepository.kt`
2. `ProblemsFragment` calls `apiService.getProblems()`
3. Bind `List<ProblemDto>` to the RecyclerView
4. Remove `USE_MOCK_API = true` from `app/build.gradle`

**Acceptance:** Problems load from Postgres via FastAPI. Empty state shows if backend down.

---

### Task 0.3 — Sandbox Run → real engine
**Owner:** Person B + Person A
**Files:** `feature/sandbox/SandboxFragment.kt:139`, `core/sandbox/CodeExecutionEngine.java`
**Steps:**
1. Replace fake "3 tests passed" with `CodeExecutionEngine.executeAsync(userCode, problem.testCasesJson, callback)`
2. Render `ExecutionResult` — pass/fail per case, stdout, timing
3. Switch editor default language to JavaScript (matches Rhino engine)

**Acceptance:** Running code against Two Sum shows real pass/fail against the JSON test cases.

---

## Priority 1 — Complete the missing engines

### Task 1.1 — Git conflict simulator
**Owner:** Person A
**Files:** `core/git/*` (currently 0-byte placeholders)
**Deliverables:**
- `GitSimulationManager.java` — orchestrates a merge conflict scenario
- `CommandParser.java` — parses `add`, `commit`, `merge`, `push`
- `GitRepositoryState.java` — in-memory repo state
- `CommandLogger.java` — logs to `GitCommandLogEntity`
- `MergeConflictGenerator.java` — creates the conflict for the UI to resolve

**Then:** Wire `GitFragment` (currently scripted) to the manager.
**Acceptance:** User enters `git merge feature` → sees a conflict → resolves it → logs the sequence.

---

### Task 1.2 — Speech recording + keyword analysis
**Owner:** Person A
**Files:** `core/speech/*` (empty), `SpeechFragment.kt`
**Deliverables:**
- `SpeechRecorder.java` — MediaRecorder wrapper, saves to `AudioFileManager`
- `SpeechRecognizerManager.java` — Android SpeechRecognizer for on-device text
- `KeywordAnalyzer.java` — computes keyword density from transcript
- `TechnicalVocabulary.java` — the vocabulary list for matching

**Then:** `SpeechFragment` records → transcribes → shows density.
**Permissions:** Request `RECORD_AUDIO` at runtime before first record.
**Acceptance:** User records 10 seconds, sees transcript + density score.

---

## Priority 2 — Backend cleanup (Person C)

### Task 2.1 — Folder restructure
**Files:** `backend/app/`
**Deliverables:**
- Move endpoints out of `main.py` into `app/api/v1/routers/*.py`
- Add `app/api/v1/api.py` aggregator
- Move logic into `app/services/*.py`
- Move DB queries into `app/crud/*.py`
- Add `app/utils/hashing.py`, `idempotency.py`, `pagination.py`

**Rule:** One router at a time. Run CI after each. Don't batch.

### Task 2.2 — Missing models
**Files:** `backend/app/models.py`
**Deliverables:**
- `Company` (company_id, email, password_hash)
- `Event` (event_id, company_id, title, event_date)
- Replace hardcoded `/events` with a DB query

### Task 2.3 — Missing core files
**Files:** `backend/app/core/`
**Deliverables:**
- `security.py` — real salted SHA-256 hashing + JWT signing
- `logging.py` — structured logging
- `exceptions.py` — custom HTTP exceptions

**Acceptance:** CI green after each task. Backend still responds to all endpoints.

---

## Priority 3 — Testing (all hands)

### Task 3.1 — Android unit tests
**Owner:** Person A + Person D
**Files:** `app/src/test/java/...`
**Targets:**
- `CodeExecutionEngineTest` — pass/fail cases, timeouts, empty suites
- `HashIdManagerTest` — same input → same hash, different inputs → different hashes
- `SyncQueueDaoTest` — insert, fetch pending, mark synced
- `ErrorParserTest` — maps HTTP codes to DomainError
- DTO deserialization tests — Gson round-trip

### Task 3.2 — Android instrumentation tests
**Owner:** Person B + Person D
**Files:** `app/src/androidTest/java/...`
**Targets:**
- Login flow end-to-end (mock + real backend)
- Offline → online sync test
- Navigation between all screens

### Task 3.3 — Backend tests
**Owner:** Person C
**Files:** `backend/tests/`
**Targets:**
- `test_auth.py` — register + login, token returned
- `test_problems.py` — list, by ID, 404
- `test_sync.py` — idempotency (same client_id twice → one row)
- `test_ai_assist.py` — 3-use limit enforced
- `test_speech.py` — transcribe returns transcript + float density

### Task 3.4 — End-to-end test plan
**Owner:** Person D
**Deliverables:**
- `docs/e2e-test-plan.md` — every scenario from the requirements
- Postman collection covering all endpoints
- Manual QA checklist for the offline→online flow

**Acceptance:** CI runs all three layers (Android unit, backend, integration) on every push.

---

## Priority 4 — Remove mocks from production build

### Task 4.1 — Audit remaining mocks
**Owner:** Person D
**Files:** Repo-wide search for `mock`, `fake`, `hardcoded`, `TODO`
**Deliverables:**
- List every mocked class/path
- For each: either wire it to the real source or delete it
- `MockApiInterceptor` stays in the repo but gated behind `USE_MOCK_API = false`

### Task 4.2 — Replace in-memory repositories
**Owner:** Person B
**Files:** `CompanyRepository`, `EventRepository`, `ProgressRepository`, `DailyChallengeManager`
**Deliverables:** Each reads from the API or Room, not static lists.

---

## Priority 5 — Documentation

### Task 5.1 — Update HANDOVER.md
**Owner:** Person D
**Add:** Current architecture diagram, what's mocked vs real, testing commands.

### Task 5.2 — Final report
**Owner:** All
**Sections per person:**
- **A:** Sandbox engine, entity design, Git simulator, speech pipeline
- **B:** UI/UX, screen flow, Kotlin-Java interop
- **C:** FastAPI architecture, schema, sync idempotency, AI limit enforcement
- **D:** Network layer, security, offline sync, CI/CD, testing infrastructure

---

## Ownership Summary Table

| Task | Owner | Priority | Deadline |
|---|---|---|---|
| Login wire | B | P0 | Week 3 |
| Problems wire | B | P0 | Week 3 |
| Sandbox Run wire | B + A | P0 | Week 3 |
| Git simulator | A | P1 | Week 3 |
| Speech pipeline | A | P1 | Week 3 |
| Backend restructure | C | P2 | Week 4 |
| Company + Event models | C | P2 | Week 4 |
| Backend security.py | C | P2 | Week 4 |
| Android unit tests | A + D | P3 | Week 4 |
| Instrumentation tests | B + D | P3 | Week 4 |
| Backend tests | C | P3 | Week 4 |
| E2E test plan | D | P3 | Week 4 |
| Mock audit | D | P4 | Week 4 |
| Repository wiring | B | P4 | Week 4 |
| Documentation | All | P5 | Week 5 |

---

## Definition of Done — Post-Demo Phase

- No screen shows hardcoded data
- Every backend endpoint is called by at least one real client
- No `TODO` comments in production paths
- CI runs all three test layers on every push
- Demo build (`USE_MOCK_API = false`) works on a physical phone against a real backend
- All mocks documented or removed
