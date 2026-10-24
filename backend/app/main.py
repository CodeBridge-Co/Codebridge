import os
from contextlib import asynccontextmanager
from datetime import datetime, timedelta
from typing import List

from fastapi import FastAPI, HTTPException, Depends, Body
from sqlalchemy import select
from sqlalchemy import func
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from jose import jwt
from passlib.context import CryptContext

from app.models import (
    Base, SyncLog, Student, Problem, AssessmentSession,
    TelemetryLog, RewardProgress, Company, Event
)
from app.schemas import (
    RegisterRequest, LoginRequest, LoginResponse,
    ProblemDto, SessionDto, SyncPayload,
    LeaderboardDto, EventDto, EventCreate, 
    AiAssistRequest, AiAssistResponse, ProblemCreate,
)

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge_db",
)

# --- JWT & Security Setup ---
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

    # 3. Seed Master Company/Admin Account
    admin_email = "admin@codebridge.com"
    result = await session.execute(select(Company).where(Company.email == admin_email))
    if result.scalars().first() is None:
        session.add(Company(
            name="CodeBridge Admin", 
            email=admin_email, 
            password_hash=pwd_context.hash("ournames"), 
            is_admin=True # This allows it to log in as BOTH Company and Admin
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
    if getattr(req, 'role', 'STUDENT') in ["COMPANY", "ADMIN"]:
        email = req.student_hash_id
        password = req.institution_id
        
        result = await db.execute(select(Company).where(Company.email == email))
        company = result.scalars().first()
        
        # Strict check for if they don't exist to reject them
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
    
@app.post("/auth/register-company", response_model=LoginResponse)
async def register_company(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    # Check if email already exists
    result = await db.execute(select(Company).where(Company.email == req.student_hash_id))
    if result.scalars().first() is not None:
        raise HTTPException(status_code=400, detail="Company already registered")
    
    # Create new company
    new_company = Company(
        name=req.institution_id, # Using institution field for Company Name
        email=req.student_hash_id, # Using student_hash_id field for Email
        password_hash=pwd_context.hash(req.password_hash), # Needs a password field!
        is_admin=False
    )
    db.add(new_company)
    await db.commit()
    await db.refresh(new_company)
    
    token = create_access_token({"sub": new_company.email, "company_id": new_company.company_id, "role": "COMPANY"})
    return LoginResponse(student_hash_id=new_company.email, token=token)

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

@app.post("/problems", response_model=ProblemDto)
async def create_problem(problem: ProblemCreate, db: AsyncSession = Depends(get_db)):
    # Generate a new ID
    result = await db.execute(select(func.max(Problem.problem_id)))
    max_id = result.scalar() or 0
    new_id = max_id + 1

    new_problem = Problem(
        problem_id=new_id,
        title=problem.title,
        difficulty_level=problem.difficulty_level,
        test_cases_json=problem.test_cases_json,
        company_id=problem.company_id
    )
    db.add(new_problem)
    await db.commit()
    await db.refresh(new_problem)
    
    return ProblemDto(
        problem_id=new_problem.problem_id,
        title=new_problem.title,
        difficulty_level=new_problem.difficulty_level,
        test_cases_json=new_problem.test_cases_json
    )

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
                        video_tracking_json=p.get("video_tracking_json", "{}"), 
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
async def get_events(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Event).order_by(Event.event_id.desc()))
    return [EventDto(event_id=e.event_id, company_id=e.company_id, title=e.title, event_date=e.event_date) for e in result.scalars().all()]

@app.post("/events", response_model=EventDto)
async def create_event(event: EventCreate, db: AsyncSession = Depends(get_db)):
    new_event = Event(company_id=event.company_id, title=event.title, event_date=event.event_date)
    db.add(new_event)
    await db.commit()
    await db.refresh(new_event)
    return EventDto(event_id=new_event.event_id, company_id=new_event.company_id, title=new_event.title, event_date=new_event.event_date)


@app.post("/ai-assist", response_model=AiAssistResponse)
async def ai_assist(req: AiAssistRequest = Body(...)):
    used = ai_usage.get(req.session_id, 0)
    if used >= AI_LIMIT:
        return AiAssistResponse(allowed=False, remaining_uses=0)
    ai_usage[req.session_id] = used + 1
    return AiAssistResponse(allowed=True, remaining_uses=AI_LIMIT - (used + 1))
