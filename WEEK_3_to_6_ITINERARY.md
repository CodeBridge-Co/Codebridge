# CodeBridge — Itinerary to Final Presentation

**Team:** Person A (Android Core), Person B (UI), Person C (Backend), Person D (Network / Security / Infra / Testing)
**Today:** Thursday, Week 3
**Final Presentation:** Week 6

---

## Key Deadlines

| Deliverable | Due |
|---|---|
| **Deliverable 4 (GitHub repo)** | Thursday, Week 4 |
| **Testing documentation** | Thursday, Week 5 |
| **Final presentation** | Week 6 |

All finalisation happens on Thursday. No weekend work counts as a deadline.

---

## Week 3 — Finish Testing (Oct 15–16)

**Goal:** Complete all P0 tasks from `TESTING_PREP.md`. No new features.

| Owner | Task | By |
|---|---|---|
| Person D | Real backend integration test, offline sync test | Thu Oct 16 |
| Person A | CodeExecutionEngine tests, TelemetrySink, TLS wiring | Thu Oct 16 |
| Person B | Register screen + push branch to origin | Thu Oct 16 |
| Person C | Expand pytest suite, Company + Event models | Thu Oct 16 |

**End of week:** All P0 tasks done. CI green.

---

## Week 4 — GitHub Deliverable + Supervisor Demo (Oct 20–24)

### Monday (Oct 20) — Lecturer Feedback

**Morning — show app to lecturer:**
- Live flow: Login → Problems → Sandbox Run → Profile
- Offline sync: WiFi off → solve → WiFi on → Postgres check
- Leaderboard, Events screens
- Be honest about what's mocked (Git, Speech, AI)

**After class — mock presentation prep with AI:**

Generate likely questions. Categories:

**Technical**
- Why SQLCipher vs plain Room?
- How does offline sync handle conflicts?
- Why Rhino instead of real Java on Android?
- How is student identity anonymised?
- Idempotency strategy for `/sync`?

**Architecture**
- Why FastAPI over Flask/Django?
- How does Android talk to the backend?
- Why SQLite for tests and Postgres for production?

**Scope / Trade-offs**
- What's mocked and why?
- Fallback if Whisper/GenAI fails?
- What was cut and what's post-MVP?

**Business**
- How do employers use this?
- How does anonymisation protect students?
- Why the AI dependency limit?

Each person prepares their section.

**Evening:** Draft answers to the top 10 questions per person.

---

### Tuesday (Oct 21) — Fix Feedback + GitHub Prep

Apply lecturer feedback from Monday. Prep the repo:
- `README.md` — project overview, how to run, tech stack, team roles
- All code pushed to `main`
- `HANDOVER.md` current
- Docs folder complete (`openapi.json`, `data-dictionary.md`, `architecture.md`, `test-plan.md`)
- CI green on all 4 jobs
- `.gitignore` correct — no `local.properties`, `.gradle/`, `build/`, `__pycache__/`

---

### Wednesday (Oct 22) — Supervisor Dry Run

Run the full flow end-to-end. Time it — 6–8 minutes max.

**Structure:**
1. Intro (30 sec) — problem, solution
2. Live demo (4 min) — login, problems, sandbox, offline sync
3. Architecture walkthrough (2 min) — diagrams
4. Q&A

Fix whatever breaks. Take notes on weak spots.

---

### Thursday (Oct 23) — **Deliverable 4 Due** + Supervisor Demo

**Morning:** Final GitHub review. Repo is clean and presentable. Push last commits.

**Deliverable 4 submitted.**

**Afternoon:** Show supervisor the live app.
- Same flow as Monday's lecturer demo
- Plus architecture diagram, ERD, CI dashboard
- Explicitly note what's post-MVP

**Take notes:** feedback becomes the Week 5 fix list.

---

### Friday (Oct 24) — Buffer

Fix anything the supervisor flagged that's quick. No new features.

---

## Week 5 — Testing Docs + Recordings (Oct 27–31)

**Deliverable due:** Testing documentation (Thursday Oct 30).

### Monday–Tuesday (Oct 27–28) — Fix Supervisor Feedback

Apply fixes from Thursday's supervisor demo. Last code changes.

---

### Wednesday (Oct 29) — Code Freeze

No pushes after today except critical bug fixes. Run full CI. All green.

---

### Wednesday–Thursday (Oct 29–30) — Recordings + Testing Docs

**Record demo video (3–4 min):**
1. Title card (10 sec) — team names, project
2. Problem statement (20 sec)
3. Live demo (3 min) — login → problems → sandbox → offline → sync → leaderboard
4. Architecture diagram (30 sec)
5. Closing (20 sec) — built vs post-MVP

**Produce database artifacts:**
- ERD diagram (final)
- Screenshot of `psql` tables with real data
- Screenshots: `sync_logs`, `assessment_sessions`, `students`, `reward_progress`
- Schema dump: `docker exec codebridge_postgres pg_dump -U codebridge_user codebridge_db > schema.sql`

**Testing documentation (Deliverable — Thu Oct 30):**
- `TESTING_PREP.md` marked complete
- Android test report (CI screenshot)
- Backend test report (pytest output)
- Integration test report (CI artifacts + local screenshots)
- Offline sync verification (before/after Postgres screenshots)
- Error handling test results
- `HANDOVER.md` final update

---

### Friday (Oct 31) — Buffer

Anything that slipped from Thursday. Final checks.

---

## Week 6 — Final Presentation (Nov 3–7)

### Monday (Nov 3) — Report Write-up

Each person writes their section:

- **Person A:** Sandbox engine, Room entities + DAOs, SQLCipher, Git simulator
- **Person B:** UI/UX architecture, Kotlin-Java interop, screen flow
- **Person C:** FastAPI architecture, SQLAlchemy models, sync idempotency, AI limit
- **Person D:** Network layer, security, offline sync, CI/CD, testing infrastructure

Each section: what you built, why, what worked, what's mocked, evidence.

---

### Tuesday–Wednesday (Nov 4–5) — Final Rehearsal + Slides

Run demo end-to-end 3 times. Fix anything broken. Everyone knows their part.

Build slide deck. Rehearse Q&A.

---

### Thursday (Nov 6) — Final Presentation

**Structure (10–12 min):**
1. Intro (1 min)
2. Live demo (4–5 min)
3. Architecture + impact (2 min)
4. Q&A (remaining)

**Have ready:**
- Backup video in case live demo fails
- Screenshots of every screen
- Architecture diagram
- ERD
- CI dashboard
- Postgres data screenshots

---

## For Android UI — Minimal Scope

| Week | Task | By |
|---|---|---|
| Week 3 | Register screen, push branch | Thu Oct 16 |
| Week 4 | UI fixes from lecturer feedback | Thu Oct 23 |
| Week 5 | Polish screens, contribute demo video | Thu Oct 30 |
| Week 6 | Present UI section | Thu Nov 6 |

---

## Critical Dates

| Date | Event | Deliverable |
|---|---|---|
| Thu Oct 16 | P0 tasks done | — |
| Mon Oct 20 | Lecturer demo + mock prep | — |
| Wed Oct 22 | Supervisor dry run | — |
| **Thu Oct 23** | **GitHub repo submitted** | **Deliverable 4** |
| Thu Oct 23 | Supervisor demo | — |
| Wed Oct 29 | Code freeze | — |
| **Thu Oct 30** | **Testing docs + recordings done** | **Testing documentation** |
| Mon Nov 3 | Report write-up | — |
| Tue–Wed Nov 4–5 | Final rehearsal + slides | — |
| **Thu Nov 6** | **Final presentation** | **Presentation** |

---

## Non-Negotiables

1. **GitHub repo clean by Thursday Oct 23.** README, commits, CI green.
2. **Testing docs done by Thursday Oct 30.** Evidence, not claims.
3. **Code freeze Wednesday Oct 29.** No pushes after. No exceptions.
4. **Record demo before writing report.** Video first, docs second.
5. **Everyone writes their own report section.** No ghost-writing.
6. **Register screen ships this week (by Thu Oct 16).** Only UI Lead's task that matters.
7. **Nothing new after Week 4 Thursday.** Only fixes and polish.
8. **No Sunday finalisation.** Thursday is the deadline. Wednesday is the target.

---

## Final Deliverables Checklist

- ✅ Working APK (installed on demo phone)
- ✅ Demo video (3–4 min)
- ✅ GitHub repo (clean, README, CI green) — **Thu Oct 23**
- ✅ Testing documentation — **Thu Oct 30**
- ✅ ERD + schema dump
- ✅ Architecture diagram
- ✅ CI dashboard screenshots
- ✅ Test reports (Android + backend + integration)
- ✅ Postgres data screenshots
- ✅ Report (per-person sections)
- ✅ Slide deck
- ✅ `HANDOVER.md` updated

---

## Ownership Summary

| Lead/Role | Primary Deliverables |
|---|---|
| **Android Core** | Sandbox engine, entities, SQLCipher, Git simulator |
| **Android UI** | UI/UX, register screen, screen polish |
| **Backend** | FastAPI backend, SQLAlchemy, endpoint tests |
| **DevSecOps** | Network, security, sync, CI/CD, testing infra, integration |
| **All** | Demo, report, presentation |
