from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()

request_counts = {}

class AIAssistRequest(BaseModel):
    session_id: str
    prompt: str

@router.post("")
def ai_assist(payload: AIAssistRequest):
    system_prompt = "provide hints not the full code"

    current_count = request_counts.get(payload.session_id, 0)
    
   if current_count < 3:
        request_counts[payload.session_id] = current_count + 1
        remaining = 3 - request_counts[payload.session_id]
        return {
            "allowed": True, 
            "system_instruction": system_prompt,  
            "hint": "Check your loop bounds.", 
            "remaining_uses": remaining
        }
        
    return {
        "allowed": False, 
        "system_instruction": system_prompt, 
        "hint": "Limit reached.", 
        "remaining_uses": 0
    }
