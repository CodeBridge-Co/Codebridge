from typing import List, Dict, Any, Optional
from pydantic import BaseModel, ConfigDict

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
    session_id: Optional[str] = None
    student_hash_id: str
    problem_id: int
    start_time: str
    end_time: Optional[str] = None
    is_offline: bool = False

class SyncItem(BaseModel):
    client_id: str
    entity_type: str
    payload: Dict[str, Any]

    model_config = ConfigDict(extra="allow")

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

class AiAssistResponse(BaseModel):
    allowed: bool
    remaining_uses: int
