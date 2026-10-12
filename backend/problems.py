from fastapi import APIRouter, HTTPException, status
from typing import List, Dict, Any

router = APIRouter()

@router.get("")
def list_problems():
    return [
        {
            "problem_id": 1,
            "title": "Two Sum",
            "difficulty_level": "Easy",
            "test_cases_json": "[{\"input\": [2,7], \"target\": 9}]"
        },
        {
            "problem_id": 2,
            "title": "Reverse String",
            "difficulty_level": "Easy",
            "test_cases_json": "[{\"input\": \"hello\", \"expected\": \"olleh\"}]"
        },
        {
            "problem_id": 3,
            "title": "Valid Parentheses",
            "difficulty_level": "Medium",
            "test_cases_json": "[{\"input\": \"()[]{}\", \"expected\": true}]"
        }
    ]

@router.get("/{problem_id}")
def get_problem_by_id(problem_id: int):
    if problem_id == 9999:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Problem not found")
    return {
        "problem_id": problem_id,
        "title": f"Sample Problem {problem_id}",
        "difficulty_level": "Medium",
        "test_cases_json": "[]"
    }