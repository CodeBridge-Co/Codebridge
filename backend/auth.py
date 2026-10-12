from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter()

class AuthResponse(BaseModel):
    token: str
    student_id: str

@router.post("/register", status_code=status.HTTP_201_CREATED)
def register_student():
    return {"status": "success", "message": "Student registered successfully"}

@router.post("/login", response_model=AuthResponse)
def login_student():
    return {"token": "mock-jwt-token-xyz", "student_id": "student-123"}