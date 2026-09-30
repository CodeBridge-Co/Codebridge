# CodeBridge: Lebo624 Handover Document

**Author:** Lebo624 (Network, Security, Data, Infra, Testing)
**Date:** 30 September 2026
**Status:** Ready for Integration

## Overview
This document outlines the deliverables, integration instructions, and outstanding action items for the work completed by Person D. 

As per the team allocation, Lebo624 owns:
- Infrastructure (`docker-compose.yml`, `env/`, CI/CD)
- Android Network (`core/network`)
- Android Security (`core/security`)
- Android Data & Sync (`core/data`, `SyncQueue`)
- Documentation & Testing Infrastructure

The goal of this handover is to allow the respective developers in charge of Android Core Engines, Android UI/UX and Backend Lead to integrate their respective modules without requiring synchronous meetings.

---

## 1. Handover for Android Core Engines

### 📦 Deliverables You Are Receiving
1. **`SqlCipherManager.java`**: Provides the `SupportFactory` for Room encryption using Android Keystore.
2. **`SyncQueueEntity.java` & `SyncQueueDao.java`**: The Room entities and DAO for the offline sync queue.
3. **`HashIdManager.java`**: Generates salted SHA-256 hash IDs for students.
4. **`SecurePreferences.java`**: Wrapper for `EncryptedSharedPreferences` used to store the hash salt securely.

### 🔗 Integration Instructions
1. **Room Database Integration:**
   - Add `SyncQueueEntity` to your `AppDatabase` class.
   - Use `SqlCipherManager.getSupportFactory(context)` when building your Room instance to enable SQLCipher encryption.
2. **`SyncWorker` Placeholder:**
   - In `core/domain/SyncWorker.java`, there is a placeholder method `getSyncQueueDao()`.
   - **Action Required:** You must replace this with your actual database instance (e.g., `AppDatabase.getInstance(context).syncQueueDao()`).
3. **HashIdManager:**
   - When registering a student, call `new HashIdManager(context).generateHashId(studentNumber)`.
   - **Note:** This class requires a `SecurePreferences` instance, which uses Android Keystore. Ensure this is initialized on a background thread if possible to avoid ANRs.

### 🚧 Outstanding Items / Blockers
- **Blocker:** `SyncWorker` will not compile until `AppDatabase` is provided by thatomoloto.
- **Action:** Please provide the `AppDatabase` class or the `SyncQueueDao` instance so we can finalize the worker.

---

## 2. Handover for Android UI/UX

### 📦 Deliverables You Are Receiving
1. **`ApiService.java` & `RetrofitClient.java`**: The network layer using Retrofit 2 and OkHttp 3.
2. **`NetworkMonitor.java`**: Exposes `LiveData<Boolean>` for online/offline state.
3. **`DataRepository.java`**: A single source of truth for your ViewModels.
4. **DTOs**: All request/response models (`ProblemDto`, `SessionDto`, etc.).
5. **`MockApiInterceptor.java`**: A tool to mock API responses for UI development.

### 🔗 Integration Instructions
1. **Network State:**
   - Inject `NetworkMonitor` into your ViewModels.
   - Observe `networkMonitor.getIsOnline()` to show sync indicators, disable online-only buttons, or display offline banners.
2. **API Calls:**
   - Use `RetrofitClient.getInstance().getApiService()` to make calls.
   - All calls return `Call<ApiResponse<T>>`. You can enqueue these and handle success/error in your UI.
3. **Building UI Without Backend (Mocking):**
   - In `RetrofitClient.java`, add this line inside the `OkHttpClient.Builder()`:
     ```java
     if (BuildConfig.DEBUG) {
         builder.addInterceptor(new MockApiInterceptor());
     }

---

## 3. Handover to Backend Lead

### 📦 Deliverables You Are Receiving
1. **`docker-compose.yml` & `env/` files**: Local development environment for PostgreSQL and FastAPI.
2. **`docs/api-contracts/openapi.json`**: The definitive API contract based on the ERD.
3. **`docs/api-contracts/postman-collection.json`**: Ready-to-import Postman collection for testing.
4. **`backend/tests/conftest.py`**: Pytest infrastructure with in-memory SQLite for fast unit testing.

### 🔗 Integration Instructions
1. **Infrastructure:**
   - Run `cd infra && docker-compose up -d` to spin up PostgreSQL and the backend container.
   - Check `infra/env/backend.env` for connection strings and JWT secrets.
2. **API Contract:**
   - **Action Required:** Please review `docs/api-contracts/openapi.json`. 
   - If your Pydantic models differ from these schemas, notify Person D immediately so the Android DTOs can be updated.
   - Freeze this contract as per the team agreement.
3. **Sync Endpoint & Idempotency:**
   - The Android app sends a `SyncPayload` containing a list of sessions and telemetry logs.
   - Each item in the queue has a unique client-generated ID. Please use this as an idempotency key in your database upsert logic to prevent duplicate entries.
4. **Backend Testing:**
   - Use `backend/tests/conftest.py` to run pytest locally without needing a full PostgreSQL instance. It uses SQLite for speed.

### 🚧 Outstanding Items / Blockers
- **Whisper API:** We are mocking the Whisper API for MVP (returning a static JSON with `transcript` and `keyword_density`). Please implement the FastAPI endpoint as a stub first, then swap it for the real API if time permits.
