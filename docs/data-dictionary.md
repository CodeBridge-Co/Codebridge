# CodeBridge Data Dictionary

This document describes the entities and attributes of the CodeBridge PostgreSQL database, as defined in the ERD.

## 1. STUDENT
Represents registered student users interacting with the mobile application.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `student_hash_id` | Varchar | PK | A salted cryptographic hash representing the student anonymously. |
| `institution_id` | Varchar | | Identifier for the university or academic institution. |
| `created_at` | Timestamp | | Timestamp recording when the profile was initialized. |

## 2. PROBLEM_SET
Stores master algorithmic challenges, descriptions, and automated test cases.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `problem_id` | Integer | PK | Unique identifier for each coding problem. |
| `title` | Varchar | | Name of the algorithmic challenge. |
| `difficulty_level` | Varchar | | Categorization level (Easy, Medium, Hard). |
| `test_cases_json` | Text | | Structured JSON payload containing unit test inputs and expected outputs. |

## 3. ASSESSMENT_SESSION
Captures individual student attempts and practice sessions.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `session_id` | Integer | PK | Unique identifier for the practice session. |
| `student_hash_id` | Varchar | FK | Links the session to a specific student. |
| `problem_id` | Integer | FK | Links the session to the targeted problem. |
| `start_time` | Timestamp | | Timestamp marking when the session started. |
| `end_time` | Timestamp | | Timestamp marking when the session ended. |
| `is_offline` | Boolean | | Flag indicating whether the session occurred while the device was offline. |

## 4. TELEMETRY_LOG
Stores multi-modal behavioral metrics and security monitoring data captured during the session.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `log_id` | Integer | PK | Unique identifier for the telemetry log. |
| `session_id` | Integer | FK | Links the metrics directly to an assessment session. |
| `execution_speed_ms` | Integer | | Time taken to compile and pass local test cases in milliseconds. |
| `correctness_score` | Float | | Percentage of test cases passed. |
| `git_error_count` | Integer | | Number of Git merge conflicts encountered. |
| `speech_keyword_density` | Float | | Verbal keyword density from Stage 3 assessment. |
| `ai_access_attempts` | Integer | | Number of GenAI assistance requests during the session. |

## 5. REWARD_PROGRESS
Tracks gamification state for each student.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `reward_id` | Integer | PK | Unique identifier for the reward tracker. |
| `student_hash_id` | Varchar | FK | Student reference. |
| `streak_count` | Integer | | Consecutive practice days. |
| `xp_points` | Integer | | Total experience points. |
| `badges_json` | Text | | Structured JSON list containing earned skill badges and timestamps. |

## 6. COMPANY
Represents employers posting events and problems.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `company_id` | Integer | PK | Unique identifier. |
| `email` | Varchar | | Employer registration email. |
| `password_hash` | Varchar | | Encrypted authentication hash. |

## 7. EVENT
Company-hosted hackathons and networking events.

| Attribute | Type | Key | Description |
| :--- | :--- | :--- | :--- |
| `event_id` | Integer | PK | Unique identifier. |
| `company_id` | Integer | FK | Company reference. |
| `title` | Varchar | | Hackathon or networking title. |
| `event_date` | Timestamp | | Scheduled date and time. |
