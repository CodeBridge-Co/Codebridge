import os
from contextlib import asynccontextmanager
from datetime import datetime, timedelta
from typing import List

from fastapi import FastAPI, HTTPException, Depends, Body
from sqlalchemy import select
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from jose import jwt
from passlib.context import CryptContext

from app.models import (
    Base, SyncLog, Student, Problem, AssessmentSession,
    TelemetryLog, RewardProgress, Company,  # Added Company
)
from app.schemas import (
    RegisterRequest, LoginRequest, LoginResponse,
    ProblemDto, SessionDto, SyncPayload,
    LeaderboardDto, EventDto,
    AiAssistRequest, AiAssistResponse,
)

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge_db",
)

# --- Real JWT & Security Setup ---
SECRET_KEY = os.getenv("SECRET_KEY", "super-secret-codebridge-key-2026")
ALGORITHM = "HS256"
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(hours=24)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
# ---------------------------------

engine = create_async_engine(DATABASE_URL, echo=False)
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)


async def get_db():
    async with AsyncSessionLocal() as session:
        yield session


async def seed_data(session: AsyncSession):
    # 1. Seed Problems
    existing = await session.execute(select(Problem))
    if existing.first() is None:
        session.add_all([
            Problem(problem_id=1, title="Two Sum", difficulty_level="Easy",
                    test_cases_json='{"cases":[{"input":"[2,7,11,15], 9","expected":"[0,1]"}]}'),
            Problem(problem_id=2, title="Reverse String", difficulty_level="Easy",
                    test_cases_json='{"cases":[{"input":"hello","expected":"olleh"}]}'),
            Problem(problem_id=3, title="Merge Intervals", difficulty_level="Medium",
                    test_cases_json='{"cases":[{"input":"[[1,3],[2,6]]","expected":"[[1,6]]"}]}'),
        ])

    # 2. Seed Demo Student
    demo = await session.get(Student, "demo-student-001")
    if demo is None:
        session.add(Student(student_hash_id="demo-student-001", institution_id="eduvos"))
        session.add(RewardProgress(student_hash_id="demo-student-001",
                                   streak_count=5, xp_points=1200, badges_json="[]"))

    # 3. Seed Admin Account
    admin_email = "admin@codebridge.com"
    result = await session.execute(select(Company).where(Company.email == admin_email))
    if result.scalars().first() is None:
        session.add(Company(
            name="CodeBridge Admin", 
            email=admin_email, 
            password_hash=pwd_context.hash("admin123"), 
            is_admin=True
        ))
        
    # 4. Seed Company Account
    company_email = "hr@techcorp.com"
    result = await session.execute(select(Company).where(Company.email == company_email))
    if result.scalars().first() is None:
        session.add(Company(
            name="TechCorp", 
            email=company_email, 
            password_hash=pwd_context.hash("password123"), 
            is_admin=False
        ))

    await session.commit()


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    async with AsyncSessionLocal() as session:
        await seed_data(session)
    yield


app = FastAPI(title="Code Bridge Backend", lifespan=lifespan)

AI_LIMIT = 3
ai_usage: dict = {}


@app.get("/")
def read_root():
    return {"status": "backend is running"}


@app.get("/health")
def health():
    return {"data": "OK", "error": None, "status": "success"}


@app.post("/auth/login", response_model=LoginResponse)
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    # 1. Handle Company/Admin (Real DB Check)
    if getattr(req, 'role', 'STUDENT') in ["COMPANY", "ADMIN"]:
        email = req.student_hash_id # Android sends email in this field
        password = req.institution_id # Android sends password in this field
        
        result = await db.execute(select(Company).where(Company.email == email))
        company = result.scalars().first()
        
        if not company or not pwd_context.verify(password, company.password_hash):
            raise HTTPException(status_code=401, detail="Invalid email or password")
            
        if req.role == "ADMIN" and not company.is_admin:
            raise HTTPException(status_code=403, detail="Not authorized as Admin")
            
        token = create_access_token({"sub": company.email, "company_id": company.company_id, "role": req.role})
        return LoginResponse(student_hash_id=company.email, token=token)

    # 2. Handle Student (Real DB Check)
    if await db.get(Student, req.student_hash_id) is None:
        db.add(Student(student_hash_id=req.student_hash_id, institution_id=req.institution_id))
        db.add(RewardProgress(student_hash_id=req.student_hash_id))
        await db.commit()
        
    token = create_access_token({"sub": req.student_hash_id, "role": "STUDENT"})
    return LoginResponse(student_hash_id=req.student_hash_id, token=token)


@app.post("/auth/register", response_model=LoginResponse)
async def register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    if await db.get(Student, req.student_hash_id) is None:
        db.add(Student(student_hash_id=req.student_hash_id, institution_id=req.institution_id))
        db.add(RewardProgress(student_hash_id=req.student_hash_id))
        await db.commit()
    token = create_access_token({"sub": req.student_hash_id, "role": "STUDENT"})
    return LoginResponse(student_hash_id=req.student_hash_id, token=token)


@app.get("/problems", response_model=List[ProblemDto])
async def get_problems(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Problem).order_by(Problem.problem_id))
    return [ProblemDto(problem_id=p.problem_id, title=p.title,
                       difficulty_level=p.difficulty_level,
                       test_cases_json=p.test_cases_json)
            for p in result.scalars().all()]


@app.get("/problems/{problem_id}", response_model=ProblemDto)
async def get_problem(problem_id: int, db: AsyncSession = Depends(get_db)):
    p = await db.get(Problem, problem_id)
    if p is None:
        raise HTTPException(status_code=404, detail="Problem not found")
    return ProblemDto(problem_id=p.problem_id, title=p.title,
                      difficulty_level=p.difficulty_level,
                      test_cases_json=p.test_cases_json)


@app.post("/sessions")
async def create_session(session: SessionDto, db: AsyncSession = Depends(get_db)):
    sid = session.session_id or f"sess-{int(datetime.utcnow().timestamp() * 1000)}"
    existing = await db.get(AssessmentSession, sid)
    if existing is None:
        db.add(AssessmentSession(
            session_id=sid,
            student_hash_id=session.student_hash_id,
            problem_id=session.problem_id,
            start_time=session.start_time,
            end_time=session.end_time,
            is_offline=1 if session.is_offline else 0,
        ))
    else:
        existing.end_time = session.end_time
    await db.commit()
    return {"session_id": sid, "status": "created"}


@app.post("/sync")
async def sync_data(payload: SyncPayload, db: AsyncSession = Depends(get_db)):
    saved = 0
    try:
        for item in payload.items:
            existing_log = await db.get(SyncLog, item.client_id)
            if existing_log is None:
                db.add(SyncLog(client_id=item.client_id,
                               entity_type=item.entity_type,
                               data=str(item.payload)))
            else:
                existing_log.data = str(item.payload)

            if item.entity_type == "session":
                p = item.payload
                if await db.get(AssessmentSession, item.client_id) is None:
                    db.add(AssessmentSession(
                        session_id=item.client_id,
                        student_hash_id=p.get("student_hash_id", ""),
                        problem_id=p.get("problem_id", 0),
                        start_time=p.get("start_time", ""),
                        end_time=p.get("end_time"),
                        is_offline=1 if p.get("is_offline", False) else 0,
                    ))
            elif item.entity_type == "telemetry":
                p = item.payload
                if await db.get(TelemetryLog, item.client_id) is None:
                    db.add(TelemetryLog(
                        log_id=item.client_id,
                        session_id=p.get("session_id", ""),
                        execution_speed_ms=p.get("execution_speed_ms", 0),
                        correctness_score=p.get("correctness_score", 0.0),
                        git_error_count=p.get("git_error_count", 0),
                        video_tracking_json=p.get("video_tracking_json", "{}"), # Replaced Speech
                        ai_access_attempts=p.get("ai_access_attempts", 0),
                    ))
            saved += 1

        await db.commit()
        return {"status": "success", "synced_items": saved}
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/leaderboard", response_model=List[LeaderboardDto])
async def get_leaderboard(db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(RewardProgress).order_by(RewardProgress.xp_points.desc()).limit(50)
    )
    return [LeaderboardDto(hash_id=r.student_hash_id, xp=r.xp_points, streak=r.streak_count)
            for r in result.scalars().all()]


@app.get("/events", response_model=List[EventDto])
async def get_events():
    return [
        EventDto(event_id=1, company_id=1, title="CodeBridge Hackathon 2026",
                 event_date="2026-06-15T09:00:00Z"),
        EventDto(event_id=2, company_id=1, title="Tech Career Fair",
                 event_date="2026-07-20T10:00:00Z"),
    ]


@app.post("/ai-assist", response_model=AiAssistResponse)
async def ai_assist(req: AiAssistRequest = Body(...)):
    used = ai_usage.get(req.session_id, 0)
    if used >= AI_LIMIT:
        return AiAssistResponse(allowed=False, remaining_uses=0)
    ai_usage[req.session_id] = used + 1
    return AiAssistResponse(allowed=True, remaining_uses=AI_LIMIT - (used + 1))

# NOTE: The /speech/transcribe mock endpoint has been completely removed as per the lecturer's feedback (No Whisper).
