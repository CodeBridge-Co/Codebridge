from fastapi import APIRouter

router = APIRouter()

@router.post("/auth/register")
async def register():
    return {
        "student_hash_id": "abc123", 
        "token": "jwt"
    }

@router.post("/auth/login")
async def login():
    return {
        "student_hash_id": "abc123", # We will change this
        "token": "jwt"
    }

@router.get("/problems")
async def get_problems():
    return [
        {
            "problem_id": 1, 
            "title": "Two Sum", 
            "difficulty_level": "Easy", 
            "test_cases_json": "..."
        }
    ]

@router.get("/problems/{problem_id}")
async def get_problem(problem_id: int):
    return {
        "problem_id": problem_id, 
        "title": "Two Sum", 
        "difficulty_level": "Easy", 
        "test_cases_json": "..."
    }

@router.post("/sessions")
async def create_session():
    return {
        "session_id": 1, 
        "status": "created"
    }

@router.post("/sync")
async def sync_data():
    return {
        "synced": 5, 
        "failed": 0
    }

@router.get("/leaderboard")
async def get_leaderboard():
    return [
        {
            "hash_id": "abc123", 
            "xp": 1200, 
            "streak": 5
        }
    ]

@router.get("/events")
async def get_events():
    return [
        {
            "event_id": 1, 
            "title": "Hackathon", 
            "company_id": 1
        }
    ]

@router.post("/speech/transcribe")
async def transcribe_speech():
    return {
        "transcript": "sample text", 
        "keyword_density": 0.4
    }

@router.post("/ai-assist")
async def ai_assist():
    return {
        "allowed": True, 
        "remaining_uses": 2
    }
