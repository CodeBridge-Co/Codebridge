from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()

@router.post("/transcribe")
def transcribe_speech():
    return {
        "transcript": "Let's iterate through the array using a for loop.",
        "keyword_density": 0.85
    }