# CodeBridge — Person A Handover (Android Core Engines)

**Author:** Thato (Person A) · **Updated:** 2026-10-05 · **Status:** Project builds; code sandbox done; Git simulator and speech pending

---

## 1. Project skeleton fixes (pull before anything else)
The skeleton was broken: empty Gradle files, no wrapper, no manifest or `res/`, and `core/` and `feature/` sat outside `app/src/main/java`, so nothing compiled. All fixed. Also fixed:
- `SyncWorker` field access, `MyApplication` imports, `SessionMapper`/`TelemetryMapper` type mismatches
- Added `AppDatabase` with all 6 entities and DAOs plus `SyncQueue`, encrypted via `SqlCipherManager`
- `MockApiInterceptor` is a **temporary passthrough stub**. Lebo: please replace it with real mock logic. Everyone else: don't build on it.

## 2. New: Code sandbox (`core/sandbox/`)
Runs user **JavaScript** (Rhino, interpreted mode) against a problem's test cases. On-device Java compilation isn't realistic on Android, so Rhino is the approved fallback (architecture doc §7.1).

**For Person B (UI):**
```java
CodeExecutionEngine engine = new CodeExecutionEngine();      // keep one instance
engine.executeAsync(userCode, problem.testCasesJson, result -> {
    // runs on a WORKER thread: post to the main thread before touching views
    runOnUiThread(() -> render(result));
});
```
`ExecutionResult` provides:
- `fatalError` — syntax error or bad suite, with no per-case results
- `timedOut`
- `results` — a list of `TestResult`, each with `status` (`PASSED`, `FAILED`, `ERROR`, `TIMEOUT`, `SKIPPED`), `input`, `expected`, `actual`, `error`, `stdout` and `durationMs`
- `passedCount()` and `allPassed()`

**Rules:**
- Time budget is 2 s for the whole run. Infinite loops are killed; the remaining cases show `SKIPPED`.
- User code must define a function: `solve`, `solution`, or the name in the suite's `"entryPoint"`. If none of those exist, the first function the code defines is used.
- `print` and `console.log` output appears in `TestResult.stdout`.

**For Person C / Lebo — `test_cases_json` format** (matches the seed data in `backend/app/main.py`):
```json
{"cases":[{"input":"[2,7,11,15], 9","expected":"[0,1]"}], "entryPoint":"twoSum"}
```
`input` is the argument list as written inside a call. A bare value such as `hello` is passed as a string. `expected` is compared after JSON normalisation. `entryPoint` is optional. **An empty suite (`"{}"`, as `MockApiInterceptor` currently returns) is reported as "no test cases"** — please give mock problems real cases.

## 3. How to work on this repo
- **Never work from OneDrive.** It silently corrupted files. Clone to `C:\Users\<you>\Dev\`.
- Create `android-app/local.properties` yourself (it is untracked): `sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`
- After every pull, and before and after every change, run from `android-app/`:
  ```
  .\gradlew.bat compileDebugJavaWithJavac
  .\gradlew.bat compileDebugAndroidTestJavaWithJavac
  .\gradlew.bat assembleDebug
  .\gradlew.bat testDebugUnitTest
  ```
  Several earlier pushes referenced classes or fields that didn't exist, so don't assume code compiles because it looks right.
- Don't add `android-app/feature/` code. The real location is `android-app/app/src/main/java/za/co/eduvos/codebridge/feature/`.

## 4. Status and next steps
| Area | Status |
|---|---|
| `core/database` | Done (encrypted Room, 6 entities and DAOs, `SyncQueue`) |
| `core/sandbox` | **Done**, 23 unit tests passing, not yet run on a device |
| `core/git` (conflict simulator) | Next (Week 3): lightweight command parser, no JGit |
| `core/speech` | Week 3. `KeywordAnalyzer` and `TechnicalVocabulary` are **not in the repo** and will be rebuilt or recovered, then wired to `SpeechRecorder` and `SpeechRecognizerManager` |

**Known gaps:** a loop that allocates memory endlessly could still exhaust memory (only time and stack are limited), and the sandbox has no on-device test yet. `.gradle/` and `build/` are tracked in git and should be added to `.gitignore`.

**Asks:**
- **Person B:** wire the sandbox into the editor and results screen using the snippet above.
- **Lebo:** replace `MockApiInterceptor` and give the mock problems real `test_cases_json`.
- **Person C:** keep `test_cases_json` in the format above.
