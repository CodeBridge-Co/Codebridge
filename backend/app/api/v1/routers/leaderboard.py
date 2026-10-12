from fastapi import APIRouter

router = APIRouter()

@router.get("")
def get_leaderboard():
    return [
        {"rank": 1, "student_name": "Alice", "score": 1250},
        {"rank": 2, "student_name": "Bob", "score": 1100}
    ]
