from pydantic import BaseModel, ConfigDict
from typing import List, Dict, Any, Optional

class LoginRequest(BaseModel):
    student_hash_id: str
    institution_id: str
    role: str = "STUDENT"

class RegisterRequest(BaseModel):
    student_hash_id: str
    institution_id: str
    password_hash: str = ""

class LoginResponse(BaseModel):
    student_hash_id: str
    token: str

class ProblemDto(BaseModel):
    problem_id: int
    title: str
    difficulty_level: str
    test_cases_json: str
    description: str = ""
    example_input: str = ""
    example_output: str = ""
    initial_code: str = ""
    company_id: Optional[int] = None 
    model_config = ConfigDict(from_attributes=True)

class ProblemCreate(BaseModel):
    title: str
    difficulty_level: str
    test_cases_json: str
    company_id: int
    description: str = ""
    example_input: str = ""
    example_output: str = ""
    initial_code: str = ""

class EventDto(BaseModel):
    event_id: int
    company_id: int
    title: str
    event_date: str
    model_config = ConfigDict(from_attributes=True)

class EventCreate(BaseModel):
    company_id: int
    title: str
    event_date: str

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

class SyncPayload(BaseModel):
    items: List[SyncItem]

class LeaderboardDto(BaseModel):
    hash_id: str
    xp: int
    streak: int

# UPDATED AI SCHEMAS
class AiAssistRequest(BaseModel):
    session_id: str
    problem_title: str = "Unknown Problem"
    current_code: str = ""

class AiAssistResponse(BaseModel):
    allowed: bool
    remaining_uses: int
    hint_text: str = ""
