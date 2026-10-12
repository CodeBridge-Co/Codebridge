from fastapi import APIRouter, status
from pydantic import BaseModel
from typing import Dict, Any

router = APIRouter()

class SessionCreate(BaseModel):
    problem_id: int
    student_id: str

@router.post("", status_code=status.HTTP_201_CREATED)
def create_session(session: SessionCreate):
    return {"session_id": "sess-uuid-abc-123", "status": "active"}

@router.get("/{session_id}")
def get_session(session_id: str):
    return {"session_id": session_id, "status": "active", "progress_data": {}}