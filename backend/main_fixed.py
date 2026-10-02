from fastapi import FastAPI, HTTPException, Body
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import sqlite3
import json
import uuid
from datetime import datetime

app = FastAPI(title="Code Bridge Backend")

DB_FILE = "local_app.db"

def init_db():
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS sync_logs (
            client_id TEXT PRIMARY KEY,
            entity_type TEXT,
            data TEXT,
            created_at TEXT
        )
    """)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS students (
            student_hash_id TEXT PRIMARY KEY,
            institution_id TEXT,
            created_at TEXT
        )
    """)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS assessment_sessions (
            session_id TEXT PRIMARY KEY,
            student_hash_id TEXT,
            problem_id INTEGER,
            start_time TEXT,
            end_time TEXT,
            is_offline INTEGER
        )
    """)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS telemetry_logs (
            log_id TEXT PRIMARY KEY,
            session_id TEXT,
            execution_speed_ms INTEGER,
            correctness_score REAL,
            git_error_count INTEGER,
            speech_keyword_density REAL,
            ai_access_attempts INTEGER
        )
    """)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS reward_progress (
            student_hash_id TEXT PRIMARY KEY,
            streak_count INTEGER DEFAULT 0,
            xp_points INTEGER DEFAULT 0,
            badges_json TEXT DEFAULT '[]'
        )
    """)
    conn.commit()
    conn.close()

init_db()

# Pydantic models

class RegisterRequest(BaseModel):
    student_hash_id: str
    institution_id: str

class LoginRequest(BaseModel):
    student_hash_id: str
    institution_id: str

class LoginResponse(BaseModel):
    student_hash_id: str
    token: str

class ProblemDto(BaseModel):
    problem_id: int
    title: str
    difficulty_level: str
    test_cases_json: str

class SessionDto(BaseModel):
    session_id: Optional[str] = None
    student_hash_id: str
    problem_id: int
    start_time: str
    end_time: Optional[str] = None
    is_offline: bool = False

class SyncItem(BaseModel):
    client_id: str
    entity_type: str
    payload: Dict[str, Any]

class SyncPayload(BaseModel):
    items: List[SyncItem]

class LeaderboardDto(BaseModel):
    hash_id: str
    xp: int
    streak: int

class EventDto(BaseModel):
    event_id: int
    company_id: int
    title: str
    event_date: str

class AiAssistResponse(BaseModel):
    allowed: bool
    remaining_uses: int

# Mock data

MOCK_PROBLEMS = [
    ProblemDto(problem_id=1, title="Two Sum", difficulty_level="Easy",
               test_cases_json=json.dumps({"cases": [{"input": "[2,7,11,15], 9", "expected": "[0,1]"}]})),
    ProblemDto(problem_id=2, title="Reverse String", difficulty_level="Easy",
               test_cases_json=json.dumps({"cases": [{"input": "hello", "expected": "olleh"}]})),
    ProblemDto(problem_id=3, title="Merge Intervals", difficulty_level="Medium",
               test_cases_json=json.dumps({"cases": [{"input": "[[1,3],[2,6]]", "expected": "[[1,6]]"}]})),
]

ai_usage: Dict[str, int] = {}
AI_LIMIT = 3

# Root & health

@app.get("/")
def read_root():
    return {"status": "backend is running"}

@app.get("/health")
def health():
    return {"data": "OK", "error": None, "status": "success"}

# Auth

@app.post("/auth/register", response_model=LoginResponse)
def register(req: RegisterRequest):
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute(
        "INSERT OR IGNORE INTO students (student_hash_id, institution_id, created_at) VALUES (?, ?, ?)",
        (req.student_hash_id, req.institution_id, datetime.utcnow().isoformat())
    )
    cursor.execute(
        "INSERT OR IGNORE INTO reward_progress (student_hash_id) VALUES (?)",
        (req.student_hash_id,)
    )
    conn.commit()
    conn.close()
    return LoginResponse(student_hash_id=req.student_hash_id, token=f"jwt-mock-{uuid.uuid4().hex[:16]}")

@app.post("/auth/login", response_model=LoginResponse)
def login(req: LoginRequest):
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute("SELECT 1 FROM students WHERE student_hash_id = ?", (req.student_hash_id,))
    if not cursor.fetchone():
        cursor.execute(
            "INSERT INTO students (student_hash_id, institution_id, created_at) VALUES (?, ?, ?)",
            (req.student_hash_id, req.institution_id, datetime.utcnow().isoformat())
        )
        cursor.execute("INSERT OR IGNORE INTO reward_progress (student_hash_id) VALUES (?)", (req.student_hash_id,))
        conn.commit()
    conn.close()
    return LoginResponse(student_hash_id=req.student_hash_id, token=f"jwt-mock-{uuid.uuid4().hex[:16]}")

# Problems

@app.get("/problems", response_model=List[ProblemDto])
def get_problems():
    return MOCK_PROBLEMS

@app.get("/problems/{problem_id}", response_model=ProblemDto)
def get_problem(problem_id: int):
    for p in MOCK_PROBLEMS:
        if p.problem_id == problem_id:
            return p
    raise HTTPException(status_code=404, detail="Problem not found")

# Sessions

@app.post("/sessions")
def create_session(session: SessionDto):
    session_id = session.session_id or f"sess-{uuid.uuid4().hex[:12]}"
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute(
        "INSERT OR REPLACE INTO assessment_sessions (session_id, student_hash_id, problem_id, start_time, end_time, is_offline) VALUES (?, ?, ?, ?, ?, ?)",
        (session_id, session.student_hash_id, session.problem_id, session.start_time, session.end_time, int(session.is_offline))
    )
    conn.commit()
    conn.close()
    return {"session_id": session_id, "status": "created"}

# Sync (idempotent)

@app.post("/sync")
def sync_data(payload: SyncPayload):
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    saved_count = 0
    try:
        for item in payload.items:
            # Idempotent log
            cursor.execute(
                "INSERT OR REPLACE INTO sync_logs (client_id, entity_type, data, created_at) VALUES (?, ?, ?, ?)",
                (item.client_id, item.entity_type, json.dumps(item.payload), datetime.utcnow().isoformat())
            )
            # Route to correct table
            if item.entity_type == "session":
                s = item.payload
                cursor.execute(
                    "INSERT OR REPLACE INTO assessment_sessions (session_id, student_hash_id, problem_id, start_time, end_time, is_offline) VALUES (?, ?, ?, ?, ?, ?)",
                    (item.client_id, s.get("student_hash_id", ""), s.get("problem_id", 0),
                     s.get("start_time", ""), s.get("end_time"), int(s.get("is_offline", False)))
                )
            elif item.entity_type == "telemetry":
                t = item.payload
                cursor.execute(
                    "INSERT OR REPLACE INTO telemetry_logs (log_id, session_id, execution_speed_ms, correctness_score, git_error_count, speech_keyword_density, ai_access_attempts) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    (item.client_id, t.get("session_id", ""), t.get("execution_speed_ms", 0),
                     t.get("correctness_score", 0.0), t.get("git_error_count", 0),
                     t.get("speech_keyword_density", 0.0), t.get("ai_access_attempts", 0))
                )
            saved_count += 1
    except Exception as e:
        conn.close()
        raise HTTPException(status_code=400, detail=str(e))
    conn.commit()
    conn.close()
    return {"status": "success", "synced_items": saved_count}

# Leaderboard

@app.get("/leaderboard", response_model=List[LeaderboardDto])
def get_leaderboard():
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute("SELECT student_hash_id, xp_points, streak_count FROM reward_progress ORDER BY xp_points DESC LIMIT 50")
    rows = cursor.fetchall()
    conn.close()
    if not rows:
        return [LeaderboardDto(hash_id="demo-hash-1", xp=1200, streak=5)]
    return [LeaderboardDto(hash_id=r[0], xp=r[1], streak=r[2]) for r in rows]

# Events

@app.get("/events", response_model=List[EventDto])
def get_events():
    return [
        EventDto(event_id=1, company_id=1, title="CodeBridge Hackathon 2024", event_date="2024-06-15T09:00:00Z"),
        EventDto(event_id=2, company_id=1, title="Tech Career Fair", event_date="2024-07-20T10:00:00Z"),
    ]

# AI Assist (server-enforced 3-use limit)

@app.post("/ai-assist", response_model=AiAssistResponse)
def ai_assist(session_id: str = Body(..., embed=True)):
    used = ai_usage.get(session_id, 0)
    if used >= AI_LIMIT:
        return AiAssistResponse(allowed=False, remaining_uses=0)
    ai_usage[session_id] = used + 1
    return AiAssistResponse(allowed=True, remaining_uses=AI_LIMIT - (used + 1))

# Whisper stub

@app.post("/speech/transcribe")
def whisper_stub():
    return {
        "transcript": "This is a mocked transcript of the assessment audio recording.",
        "keyword_density": 0.4
              }
