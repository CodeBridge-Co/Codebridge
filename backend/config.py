from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    PROJECT_NAME: str = "CodeBridge Platform API"
    DATABASE_URL: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/codebridge"
    CORS_ORIGINS: List[str] = ["*"]

    class Config:
        case_sensitive = True

settings = Settings()
