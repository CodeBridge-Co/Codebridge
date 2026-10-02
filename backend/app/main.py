from fastapi import FastAPI, HTTPException, Depends, Body
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy import select
from typing import List, Dict, Any
import json
import uuid
from datetime import datetime

from app.models import SyncLog, Student, AssessmentSession, TelemetryLog, RewardProgress
from app.schemas import (
    RegisterRequest, LoginRequest, LoginResponse, ProblemDto, 
    SessionDto, SyncPayload, LeaderboardDto, EventDto, AiAssistResponse
)
from app.db.init_db import init_db, get_db

app = FastAPI(title="Code Bridge Backend")

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

@app.on_event("startup")
async def on_startup():
    await init_db()

@app.get("/")
def read_root():
    return {"status": "backend is running"}

@app.get("/health")
def health():
    return {"data": "OK", "error": None, "status": "success"}

@app.post("/auth/register", response_model=LoginResponse)
async def register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    stmt_student = insert(Student).values(
        student_hash_id=req.student_hash_id,
        institution_id=req.institution_id,
        created_at=datetime.utcnow().isoformat()
    ).on_conflict_do_update(
        index_elements=['student_hash_id'],
        set_=dict(institution_id=req.institution_id)
    )
    stmt_reward = insert(RewardProgress).values(
        student_hash_id=req.student_hash_id
    ).on_conflict_do_nothing(index_elements=['student_hash_id'])
    
    await db.execute(stmt_student)
    await db.execute(stmt_reward)
    await db.commit()
    
    return LoginResponse(student_hash_id=req.student_hash_id, token=f"jwt-mock-{uuid.uuid4().hex[:16]}")

@app.post("/auth/login", response_model=LoginResponse)
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Student).where(Student.student_hash_id == req.student_hash_id))
    student = result.scalars().first()
    
    if not student:
        stmt_student = insert(Student).values(
            student_hash_id=req.student_hash_id,
            institution_id=req.institution_id,
            created_at=datetime.utcnow().isoformat()
        ).on_conflict_do_nothing(index_elements=['student_hash_id'])
        stmt_reward = insert(RewardProgress).values(
            student_hash_id=req.student_hash_id
        ).on_conflict_do_nothing(index_elements=['student_hash_id'])
        
        await db.execute(stmt_student)
        await db.execute(stmt_reward)
        await db.commit()
        
    return LoginResponse(student_hash_id=req.student_hash_id, token=f"jwt-mock-{uuid.uuid4().hex[:16]}")

@app.get("/problems", response_model=List[ProblemDto])
def get_problems():
    return MOCK_PROBLEMS

@app.get("/problems/{problem_id}", response_model=ProblemDto)
def get_problem(problem_id: int):
    for p in MOCK_PROBLEMS:
        if p.problem_id == problem_id:
            return p
    raise HTTPException(status_code=404, detail="Problem not found")

@app.post("/sessions")
async def create_session(session: SessionDto, db: AsyncSession = Depends(get_db)):
    session_id = session.session_id or f"sess-{uuid.uuid4().hex[:12]}"
    stmt = insert(AssessmentSession).values(
        session_id=session_id,
        student_hash_id=session.student_hash_id,
        problem_id=session.problem_id,
        start_time=session.start_time,
        end_time=session.end_time,
        is_offline=int(session.is_offline)
    ).on_conflict_do_update(
        index_elements=['session_id'],
        set_=dict(
            end_time=session.end_time,
            is_offline=int(session.is_offline)
        )
    )
    await db.execute(stmt)
    await db.commit()
    return {"session_id": session_id, "status": "created"}

@app.post("/sync")
async def sync_data(payload: SyncPayload, db: AsyncSession = Depends(get_db)):
    saved_count = 0
    try:
        for item in payload.items:
            stmt_log = insert(SyncLog).values(
                client_id=item.client_id,
                entity_type=item.entity_type,
                data=json.dumps(item.payload),
                created_at=datetime.utcnow().isoformat()
            ).on_conflict_do_update(
                index_elements=['client_id'],
                set_=dict(data=json.dumps(item.payload), entity_type=item.entity_type)
            )
            await db.execute(stmt_log)

            if item.entity_type == "session":
                s = item.payload
                stmt_sess = insert(AssessmentSession).values(
                    session_id=item.client_id,
                    student_hash_id=s.get("student_hash_id", ""),
                    problem_id=s.get("problem_id", 0),
                    start_time=s.get("start_time", ""),
                    end_time=s.get("end_time"),
                    is_offline=int(s.get("is_offline", False))
                ).on_conflict_do_update(
                    index_elements=['session_id'],
                    set_=dict(end_time=s.get("end_time"), is_offline=int(s.get("is_offline", False)))
                )
                await db.execute(stmt_sess)
            elif item.entity_type == "telemetry":
                t = item.payload
                stmt_tel = insert(TelemetryLog).values(
                    log_id=item.client_id,
                    session_id=t.get("session_id", ""),
                    execution_speed_ms=t.get("execution_speed_ms", 0),
                    correctness_score=t.get("correctness_score", 0.0),
                    git_error_count=t.get("git_error_count", 0),
                    speech_keyword_density=t.get("speech_keyword_density", 0.0),
                    ai_access_attempts=t.get("ai_access_attempts", 0)
                ).on_conflict_do_update(
                    index_elements=['log_id'],
                    set_=dict(
                        execution_speed_ms=t.get("execution_speed_ms", 0),
                        correctness_score=t.get("correctness_score", 0.0),
                        git_error_count=t.get("git_error_count", 0),
                        speech_keyword_density=t.get("speech_keyword_density", 0.0),
                        ai_access_attempts=t.get("ai_access_attempts", 0)
                    )
                )
                await db.execute(stmt_tel)
            saved_count += 1

        await db.commit()
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=400, detail=str(e))
    
    return {"status": "success", "synced_items": saved_count}

@app.get("/leaderboard", response_model=List[LeaderboardDto])
async def get_leaderboard(db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(RewardProgress.student_hash_id, RewardProgress.xp_points, RewardProgress.streak_count)
        .order_by(RewardProgress.xp_points.desc())
        .limit(50)
    )
    rows = result.all()
    if not rows:
        return [LeaderboardDto(hash_id="demo-hash-1", xp=1200, streak=5)]
    return [LeaderboardDto(hash_id=r[0], xp=r[1], streak=r[2]) for r in rows]

@app.get("/events", response_model=List[EventDto])
def get_events():
    return [
        EventDto(event_id=1, company_id=1, title="CodeBridge Hackathon 2026", event_date="2026-06-15T09:00:00Z"),
        EventDto(event_id=2, company_id=1, title="Tech Career Fair", event_date="2026-07-20T10:00:00Z"),
    ]

@app.post("/ai-assist", response_model=AiAssistResponse)
def ai_assist(session_id: str = Body(..., embed=True)):
    used = ai_usage.get(session_id, 0)
    if used >= AI_LIMIT:
        return AiAssistResponse(allowed=False, remaining_uses=0)
    ai_usage[session_id] = used + 1
    return AiAssistResponse(allowed=True, remaining_uses=AI_LIMIT - (used + 1))

@app.post("/speech/transcribe")
def whisper_stub():
    return {
        "transcript": "This is a mocked transcript of the assessment audio recording.",
        "keyword_density": 0.4
    }
