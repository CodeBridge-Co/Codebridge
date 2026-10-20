# CodeBridge: Post-Mock Integration Plan

**Status:** Infrastructure, Database, CI/CD, and Network Layers are LIVE.
**Goal:** Eliminate all mock data. Wire the Android app to the real FastAPI/PostgreSQL backend. Implement role-based routing.

---

## 🚨 The Lecturer Feedback Pivot (Read First)
Based on recent feedback, the following architectural changes are mandatory:
1. **No More Whisper (Audio):** Stage 3 is no longer audio transcription. It is now **Video/Camera tracking** (CameraX).
2. **Roles:** We now have 3 distinct roles: `STUDENT`, `COMPANY`, and `ADMIN`.
3. **Admin/Company Uploads:** Companies can upload their own specific challenges. 
4. **AI:** Must use "Prompt Proofing" (strict system prompts), no fine-tuning.

---

## 🏗️ What is Already Done (By Person D)
- **Backend & DB:** Running via Docker (`docker-compose up -d`). Seeded with initial data.
- **CI/CD:** GitHub Actions passing all Android unit tests, backend tests, and integration tests.
- **Android Network Layer:** Retrofit, OkHttp, and `ApiService` are fully configured to talk to `http://10.0.2.2:8000/`.
- **Security:** `HashIdManager`, `SqlCipher`, and `SecurePreferences` built.
- **Telemetry:** `AppSwitchDetector` and `ClipboardMonitor` are built. 

---

## 🛠️ Action Plan by Role (Next Steps)

### 👤 Person D (Infra/Network - YOU)
*Even if the UI lead is out, we must build the entry points to unblock the app.*
- [ ] **Build `AuthActivity`:** Single screen with a `RadioGroup` (Student/Company/Admin), Email, Password, and Login/Register buttons.
- [ ] **Implement Routing Logic:** On successful login, save JWT to `SecurePreferences`, read the `role` from the API response, and route to:
  - `StudentDashboardActivity`
  - `CompanyDashboardActivity`
  - `AdminDashboardActivity`
- [ ] **Build the Dashboards (Bare-bones MVP):**
  - `StudentDashboardActivity`: A simple `RecyclerView` fetching real problems via `GET /problems`.
  - `CompanyDashboardActivity` & `AdminDashboardActivity`: A `WebView` loading `http://10.0.2.2:8000/docs` (Swagger UI). This acts as the Admin portal for CRUD operations, keeping web frontends out of scope.
- [ ] **Fix Cleartext Traffic:** Ensure `android:usesCleartextTraffic="true"` is in `AndroidManifest.xml`.

### 🧠 Person C (Backend Lead - URGENT)
*The mobile app needs these endpoints to function.*
- [ ] **Update `/login` Endpoint:** Must accept `role` in the payload. 
  - If `STUDENT`, check `STUDENT` table.
  - If `COMPANY`, check `COMPANY` table.
  - If `ADMIN`, hardcode a fallback (e.g., `admin@codebridge.com` / `admin123`) or create an `ADMIN` table.
- [ ] **Update `PROBLEM_SET` Schema:** Add `company_id` (Foreign Key) so companies can post specific challenges. Null `company_id` = generic admin challenge.
- [ ] **AI Prompt Proofing:** Ensure the GenAI endpoint uses a strict system prompt (e.g., "Do not give the full code, only hints").
- [ ] **Seed Test Data:** Ensure there is a test Student and a test Company in the database seed script.

### 🎨 Person B (UI/UX Lead)
*The app needs the screens from the wireframes.*
- [ ] **Stage 1 (Code Sandbox UI):** Build the layout with the code editor, test case pass/fail dots, and the AI assist limit tracker.
- [ ] **Stage 3 (Video Explanation UI):** Replace the Audio/Mic UI with a **CameraX Preview** layout.
- [ ] **Events & Leaderboard:** Build the lists and bind them to the real `GET /events` and `GET /leaderboard` endpoints.
- [ ] **Offline UI:** Add a visual banner that says "Offline Mode - Syncing later" using `NetworkMonitor`.

### ⚙️ Person A (Core Engines Lead)
*The heavy lifting of the device features.*
- [ ] **Stage 1 Execution:** Implement the local Java sandbox for the code editor.
- [ ] **Stage 3 Camera:** Scrap the SpeechRecognizer. Implement **CameraX** and basic ML Kit face tracking/gaze detection.
- [ ] **Git Simulator:** Implement the JGit conflict resolution terminal.
- [ ] **Telemetry Formatting:** Ensure the output of the Camera tracking and Git errors are formatted into the `VideoTelemetryDto` and `TelemetryLogDto` for Person D's Sync Queue.

---

## 🚀 How to Test the Integration (For Everyone)
1. Open terminal, navigate to `infra/`, run: `docker compose up -d`
2. Open Android Studio, run the app on an Emulator (API 26+).
3. Log in as **Student** (Check DB for seeded credentials).
4. Verify problems load from the backend.
5. Log in as **Company** or **Admin**.
6. Verify the Swagger UI loads in the WebView, allowing you to POST a new challenge.

## ⚠️ Known Blockers
* **Android Emulator Network:** If the app cannot connect to `localhost`, ensure you are using `http://10.0.2.2:8000/` in Retrofit.
* **CORS Issues:** If Swagger UI in the WebView acts weird, check the FastAPI `CORS_ORIGINS` in `infra/env/backend.env`.

**Rule of thumb:** Do not merge to `develop` unless `./gradlew test` and `pytest` pass locally. The CI pipeline is the source of truth.
