import json
import os
from contextlib import asynccontextmanager
from typing import List, Dict, Any
from datetime import datetime

from fastapi import FastAPI, HTTPException, Depends, Body
from pydantic import BaseModel
from sqlalchemy import text
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge_db"
)

engine = create_async_engine(DATABASE_URL, echo=False)
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)


async def get_db():
    async with AsyncSessionLocal() as session:
        yield session


async def init_schema(conn):
    await conn.execute(text("""
        CREATE TABLE IF NOT EXISTS sync_logs (
            client_id VARCHAR PRIMARY KEY,
            entity_type VARCHAR,
            data TEXT,
            created_at TIMESTAMP DEFAULT NOW()
        );
    """))
    await conn.execute(text("""
        CREATE TABLE IF NOT EXISTS students (
            student_hash_id VARCHAR PRIMARY KEY,
            institution_id VARCHAR,
            created_at TIMESTAMP DEFAULT NOW()
        );
    """))
    await conn.execute(text("""
        CREATE TABLE IF NOT EXISTS problems (
            problem_id INTEGER PRIMARY KEY,
            title VARCHAR,
            difficulty_level VARCHAR,
            test_cases_json TEXT
        );
    """))
    await conn.execute(text("""
        CREATE TABLE IF NOT EXISTS assessment_sessions (
            session_id VARCHAR PRIMARY KEY,
            student_hash_id VARCHAR,
            problem_id INTEGER,
            start_time TIMESTAMP,
            end_time TIMESTAMP,
            is_offline BOOLEAN DEFAULT FALSE
        );
    """))
    await conn.execute(text("""
        CREATE TABLE IF NOT EXISTS telemetry_logs (
            log_id SERIAL PRIMARY KEY,
            client_id VARCHAR UNIQUE,
            session_id VARCHAR,
            execution_speed_ms INTEGER,
            correctness_score REAL,
            git_error_count INTEGER,
            speech_keyword_density REAL,
            ai_access_attempts INTEGER
        );
    """))
    await conn.execute(text("""
        CREATE TABLE IF NOT EXISTS reward_progress (
            student_hash_id VARCHAR PRIMARY KEY,
            streak_count INTEGER DEFAULT 0,
            xp_points INTEGER DEFAULT 0,
            badges_json TEXT DEFAULT '[]'
        );
    """))


async def seed_data(conn):
    problems = [
        (1, "Two Sum", "Easy", json.dumps({"cases": [{"input": "[2,7,11,15], 9", "expected": "[0,1]"}]})),
        (2, "Reverse String", "Easy", json.dumps({"cases": [{"input": "hello", "expected": "olleh"}]})),
        (3, "Merge Intervals", "Medium", json.dumps({"cases": [{"input": "[[1,3],[2,6]]", "expected": "[[1,6]]"}]})),
    ]
    for pid, title, diff, cases in problems:
        await conn.execute(text("""
            INSERT INTO problems (problem_id, title, difficulty_level, test_cases_json)
            VALUES (:pid, :title, :diff, :cases)
            ON CONFLICT (problem_id) DO NOTHING;
        """), {"pid": pid, "title": title, "diff": diff, "cases": cases})

    await conn.execute(text("""
        INSERT INTO students (student_hash_id, institution_id)
        VALUES ('demo-student-001', 'eduvos')
        ON CONFLICT (student_hash_id) DO NOTHING;
    """))
    await conn.execute(text("""
        INSERT INTO reward_progress (student_hash_id, streak_count, xp_points, badges_json)
        VALUES ('demo-student-001', 5, 1200, '[]')
        ON CONFLICT (student_hash_id) DO NOTHING;
    """))


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await init_schema(conn)
        await seed_data(conn)
    yield


app = FastAPI(title="Code Bridge Backend", lifespan=lifespan)


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
    session_id: str | None = None
    student_hash_id: str
    problem_id: int
    start_time: str
    end_time: str | None = None
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

class AiAssistRequest(BaseModel):
    session_id: str

class AiAssistResponse(BaseModel):
    allowed: bool
    remaining_uses: int

class SpeechResult(BaseModel):
    transcript: str
    keyword_density: float


AI_LIMIT = 3
ai_usage: Dict[str, int] = {}


@app.get("/")
def read_root():
    return {"status": "backend is running"}


@app.get("/health")
def health():
    return {"data": "OK", "error": None, "status": "success"}


@app.post("/auth/register", response_model=LoginResponse)
async def register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    await db.execute(text("""
        INSERT INTO students (student_hash_id, institution_id)
        VALUES (:hash, :inst)
        ON CONFLICT (student_hash_id) DO NOTHING;
    """), {"hash": req.student_hash_id, "inst": req.institution_id})
    await db.execute(text("""
        INSERT INTO reward_progress (student_hash_id) VALUES (:hash)
        ON CONFLICT (student_hash_id) DO NOTHING;
    """), {"hash": req.student_hash_id})
    await db.commit()
    return LoginResponse(student_hash_id=req.student_hash_id, token="jwt-mock-token")


@app.post("/auth/login", response_model=LoginResponse)
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    await db.execute(text("""
        INSERT INTO students (student_hash_id, institution_id)
        VALUES (:hash, :inst)
        ON CONFLICT (student_hash_id) DO NOTHING;
    """), {"hash": req.student_hash_id, "inst": req.institution_id})
    await db.execute(text("""
        INSERT INTO reward_progress (student_hash_id) VALUES (:hash)
        ON CONFLICT (student_hash_id) DO NOTHING;
    """), {"hash": req.student_hash_id})
    await db.commit()
    return LoginResponse(student_hash_id=req.student_hash_id, token="jwt-mock-token")


@app.get("/problems", response_model=List[ProblemDto])
async def get_problems(db: AsyncSession = Depends(get_db)):
    result = await db.execute(text(
        "SELECT problem_id, title, difficulty_level, test_cases_json FROM problems ORDER BY problem_id"
    ))
    return [ProblemDto(problem_id=r[0], title=r[1], difficulty_level=r[2], test_cases_json=r[3])
            for r in result.fetchall()]


@app.get("/problems/{problem_id}", response_model=ProblemDto)
async def get_problem(problem_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(text(
        "SELECT problem_id, title, difficulty_level, test_cases_json FROM problems WHERE problem_id = :pid"
    ), {"pid": problem_id})
    row = result.fetchone()
    if not row:
        raise HTTPException(status_code=404, detail="Problem not found")
    return ProblemDto(problem_id=row[0], title=row[1], difficulty_level=row[2], test_cases_json=row[3])


@app.post("/sessions")
async def create_session(session: SessionDto, db: AsyncSession = Depends(get_db)):
    sid = session.session_id or f"sess-{int(datetime.utcnow().timestamp() * 1000)}"
    await db.execute(text("""
        INSERT INTO assessment_sessions (session_id, student_hash_id, problem_id, start_time, end_time, is_offline)
        VALUES (:sid, :hash, :pid, :start, :end, :offline)
        ON CONFLICT (session_id) DO UPDATE SET end_time = EXCLUDED.end_time;
    """), {
        "sid": sid,
        "hash": session.student_hash_id,
        "pid": session.problem_id,
        "start": session.start_time,
        "end": session.end_time,
        "offline": session.is_offline,
    })
    await db.commit()
    return {"session_id": sid, "status": "created"}


@app.post("/sync")
async def sync_data(payload: SyncPayload, db: AsyncSession = Depends(get_db)):
    saved = 0
    try:
        for item in payload.items:
            await db.execute(text("""
                INSERT INTO sync_logs (client_id, entity_type, data)
                VALUES (:cid, :etype, :data)
                ON CONFLICT (client_id) DO UPDATE SET data = EXCLUDED.data;
            """), {
                "cid": item.client_id,
                "etype": item.entity_type,
                "data": json.dumps(item.payload),
            })

            if item.entity_type == "session":
                p = item.payload
                await db.execute(text("""
                    INSERT INTO assessment_sessions (session_id, student_hash_id, problem_id, start_time, end_time, is_offline)
                    VALUES (:sid, :hash, :pid, :start, :end, :offline)
                    ON CONFLICT (session_id) DO NOTHING;
                """), {
                    "sid": item.client_id,
                    "hash": p.get("student_hash_id", ""),
                    "pid": p.get("problem_id", 0),
                    "start": p.get("start_time"),
                    "end": p.get("end_time"),
                    "offline": bool(p.get("is_offline", False)),
                })
            elif item.entity_type == "telemetry":
                p = item.payload
                await db.execute(text("""
                    INSERT INTO telemetry_logs (client_id, session_id, execution_speed_ms, correctness_score, git_error_count, speech_keyword_density, ai_access_attempts)
                    VALUES (:cid, :sid, :speed, :correct, :giterr, :speech, :ai)
                    ON CONFLICT (client_id) DO NOTHING;
                """), {
                    "cid": item.client_id,
                    "sid": p.get("session_id", ""),
                    "speed": p.get("execution_speed_ms", 0),
                    "correct": p.get("correctness_score", 0.0),
                    "giterr": p.get("git_error_count", 0),
                    "speech": p.get("speech_keyword_density", 0.0),
                    "ai": p.get("ai_access_attempts", 0),
                })
            saved += 1

        await db.commit()
        return {"status": "success", "synced_items": saved}
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/leaderboard", response_model=List[LeaderboardDto])
async def get_leaderboard(db: AsyncSession = Depends(get_db)):
    result = await db.execute(text(
        "SELECT student_hash_id, xp_points, streak_count FROM reward_progress ORDER BY xp_points DESC LIMIT 50"
    ))
    return [LeaderboardDto(hash_id=r[0], xp=r[1], streak=r[2]) for r in result.fetchall()]


@app.get("/events", response_model=List[EventDto])
async def get_events():
    return [
        EventDto(event_id=1, company_id=1, title="CodeBridge Hackathon 2026", event_date="2026-06-15T09:00:00Z"),
        EventDto(event_id=2, company_id=1, title="Tech Career Fair", event_date="2026-07-20T10:00:00Z"),
    ]


@app.post("/ai-assist", response_model=AiAssistResponse)
async def ai_assist(req: AiAssistRequest = Body(...)):
    used = ai_usage.get(req.session_id, 0)
    if used >= AI_LIMIT:
        return AiAssistResponse(allowed=False, remaining_uses=0)
    ai_usage[req.session_id] = used + 1
    return AiAssistResponse(allowed=True, remaining_uses=AI_LIMIT - (used + 1))


@app.post("/speech/transcribe", response_model=SpeechResult)
async def transcribe():
    return SpeechResult(
        transcript="This is a mocked transcript of the assessment audio recording.",
        keyword_density=0.4,
    )
