from fastapi.testclient import TestClient
from main import app  # or from app.main import app depending on your setup

client = TestClient(app)

def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "success"

def test_root():
    r = client.get("/")
    assert r.status_code == 200
    assert r.json()["status"] == "backend is running"

def test_problems_list():
    r = client.get("/problems")
    assert r.status_code == 200
    assert len(r.json()) >= 3
    for p in r.json():
        assert set(p.keys()) == {"problem_id", "title", "difficulty_level", "test_cases_json"}

def test_problem_by_id():
    r = client.get("/problems/1")
    assert r.status_code == 200
    assert r.json()["problem_id"] == 1

def test_problem_not_found():
    r = client.get("/problems/9999")
    assert r.status_code == 404

def test_leaderboard():
    r = client.get("/leaderboard")
    assert r.status_code == 200

def test_events():
    r = client.get("/events")
    assert r.status_code == 200
    assert len(r.json()) >= 1

def test_speech_transcribe():
    r = client.post("/speech/transcribe")
    assert r.status_code == 200
    body = r.json()
    assert "transcript" in body
    assert isinstance(body["keyword_density"], float)

def test_ai_assist_limit():
    for i in range(3):
        r = client.post("/ai-assist", json={"session_id": "test-sess-1"})
        assert r.status_code == 200
        assert r.json()["allowed"] is True
        
    r = client.post("/ai-assist", json={"session_id": "test-sess-1"})
    assert r.status_code == 200
    assert r.json()["allowed"] is False
    assert r.json()["remaining_uses"] == 0

