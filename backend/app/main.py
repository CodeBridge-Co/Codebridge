import os
from typing import List, Dict, Any
from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy import text

DATABASE_URL = os.getenv(
    "DATABASE_URL", 
    "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge"
)

engine = create_async_engine(DATABASE_URL, echo=True)
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)

app = FastAPI(title="Code Bridge Backend")

async def get_db():
    async with AsyncSessionLocal() as session:
        yield session

@app.on_event("startup")
async def startup_db():
    """Ensure the sync_logs table exists in PostgreSQL on startup."""
    async with engine.begin() as conn:
        await conn.execute(text("""
            CREATE TABLE IF NOT EXISTS sync_logs (
                client_id VARCHAR PRIMARY KEY,
                data TEXT
            );
        """))

class SyncItem(BaseModel, extra="allow"):
    client_id: str
    payload: Dict[str, Any]

class SyncPayload(BaseModel):
    items: List[SyncItem]

@app.get("/")
def read_root():
    return {"status": "backend is running"}

@app.post("/sync")
async def sync_data(payload: SyncPayload, db: AsyncSession = Depends(get_db)):
    """
    Sync endpoint with PostgreSQL idempotency.
    Uses ON CONFLICT (client_id) DO UPDATE (upsert) to handle retries without duplicate errors.
    """
    saved_count = 0
    try:
        for item in payload.items:
            # PostgreSQL upsert query matching the SQLite logic
            query = text("""
                INSERT INTO sync_logs (client_id, data)
                VALUES (:client_id, :data)
                ON CONFLICT (client_id) 
                DO UPDATE SET data = EXCLUDED.data;
            """)
            await db.execute(query, {"client_id": item.client_id, "data": str(item.payload)})
            saved_count += 1
        
        await db.commit()
        return {"status": "success", "synced_items": saved_count}
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/whisper-stub")
def whisper_stub():
    """
    MVP Whisper API Stub returning static JSON.
    """
    return {
        "transcript": "This is a mocked transcript of the assessment audio recording.",
        "keyword_density": {
            "competency": 0.05,
            "assessment": 0.03,
            "system": 0.02
        }
    }
