from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()

# Simple in-memory rate limit tracker for testing
request_counts = {}

class AIAssistRequest(BaseModel):
    session_id: str

@router.post("")
def ai_assist(payload: AIAssistRequest):
    current_count = request_counts.get(payload.session_id, 0)
    
    if current_count < 3:
        request_counts[payload.session_id] = current_count + 1
        remaining = 3 - request_counts[payload.session_id]
        return {"allowed": True, "hint": "Check your loop bounds.", "remaining_uses": remaining}
    else:
        return {"allowed": False, "hint": "Limit reached.", "remaining_uses": 0}