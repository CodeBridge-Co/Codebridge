import os
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from app.models import Base, Student, Company
from sqlalchemy import select

DATABASE_URL = os.getenv(
    "DATABASE_URL", 
    "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge"
)

engine = create_async_engine(DATABASE_URL, echo=True)
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        
    async with AsyncSessionLocal() as session:
        async with session.begin():
            company_result = await session.execute(select(Company).where(Company.company_id == 1))
            if not company_result.scalar_one_or_none():
                session.add(Company(company_id=1, name="CodeBridge Partner Corp"))

            student_result = await session.execute(select(Student).where(Student.student_hash_id == "test-student-123"))
            if not student_result.scalar_one_or_none():
                session.add(Student(student_hash_id="test-student-123", institution_id="Demo University"))
                
        await session.commit()

async def get_db():
    async with AsyncSessionLocal() as session:
        yield session
