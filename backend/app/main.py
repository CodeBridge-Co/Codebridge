from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any
import sqlite3

app = FastAPI(title="Code Bridge Backend")

DB_FILE = "local_app.db"

def init_db():
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS sync_logs (
            client_id TEXT PRIMARY KEY,
            data TEXT
        )
    """)
    conn.commit()
    conn.close()

init_db()

class SyncItem(BaseModel, extra="allow"):
    client_id: str
    payload: Dict[str, Any]

class SyncPayload(BaseModel):
    items: List[SyncItem]


@app.get("/")
def read_root():
    return {"status": "backend is running"}

@app.post("/sync")
def sync_data(payload: SyncPayload):
    """
    Sync endpoint with idempotency. 
    Uses client_id as the key to upsert (insert or replace) so duplicates don't create new rows.
    """
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    saved_count = 0
    for item in payload.items:
        try:
            cursor.execute(
                "INSERT OR REPLACE INTO sync_logs (client_id, data) VALUES (?, ?)",
                (item.client_id, str(item.payload))
            )
            saved_count += 1
        except Exception as e:
            conn.close()
            raise HTTPException(status_code=400, detail=str(e))
            
    conn.commit()
    conn.close()
    return {"status": "success", "synced_items": saved_count}

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
