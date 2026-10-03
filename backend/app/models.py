from sqlalchemy import Column, String, Text, DateTime, Integer, Float, func
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass

class SyncLog(Base):
    __tablename__ = "sync_logs"
    client_id = Column(String, primary_key=True, index=True)
    entity_type = Column(String)
    data = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class Student(Base):
    __tablename__ = "students"
    student_hash_id = Column(String, primary_key=True, index=True)
    institution_id = Column(String)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

# Added Problem model for problem/ endpoints
class Problem(Base):
    __tablename__ = "problems"
    problem_id = Column(Integer, primary_key=True, index=True)
    title = Column(String)
    difficulty_level = Column(String)
    test_cases_json = Column(Text)

class AssessmentSession(Base):
    __tablename__ = "assessment_sessions"
    session_id = Column(String, primary_key=True, index=True)
    student_hash_id = Column(String)
    problem_id = Column(Integer)
    start_time = Column(String)
    end_time = Column(String, nullable=True)
    is_offline = Column(Integer, default=0)

class TelemetryLog(Base):
    __tablename__ = "telemetry_logs"
    log_id = Column(String, primary_key=True, index=True)
    session_id = Column(String)
    execution_speed_ms = Column(Integer)
    correctness_score = Column(Float)
    git_error_count = Column(Integer)
    speech_keyword_density = Column(Float)
    ai_access_attempts = Column(Integer)

class RewardProgress(Base):
    __tablename__ = "reward_progress"
    student_hash_id = Column(String, primary_key=True, index=True)
    streak_count = Column(Integer, default=0)
    xp_points = Column(Integer, default=0)
    badges_json = Column(Text, default="[]")
