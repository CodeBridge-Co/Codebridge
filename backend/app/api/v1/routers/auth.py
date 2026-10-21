from fastapi import APIRouter, status
from pydantic import BaseModel

router = APIRouter()

class LoginRequest(BaseModel):
    email: str
    password: str
    role: str  # "STUDENT", "COMPANY", or "ADMIN"

@router.post("/login")
def login(payload: LoginRequest):
    role = payload.role.upper()
    
    if role == "STUDENT":
        # TODO: Query STUDENT table
        return {"status": "success", "role": "STUDENT", "token": "mock-student-jwt"}
        
    elif role == "COMPANY":
        # TODO: Query COMPANY table
        return {"status": "success", "role": "COMPANY", "token": "mock-company-jwt"}
        
    elif role == "ADMIN":
        if payload.email == "admin@codebridge.com" and payload.password == "admin123":
            return {"status": "success", "role": "ADMIN", "token": "mock-admin-jwt"}
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid admin credentials")
        
    raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid role specified")
