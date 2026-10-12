from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import List


class Settings(BaseSettings):
    PROJECT_NAME: str = "CodeBridge Platform API"

    # Default matches docker-compose's postgres service name and credentials.
    # Overridden by env/backend.env when running via Docker.
    DATABASE_URL: str = "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge_db"

    SECRET_KEY: str = "dev-secret"
    HASH_SALT: str = "dev-salt"

    CORS_ORIGINS: List[str] = ["*"]

    model_config = SettingsConfigDict(
        case_sensitive=True,
        env_file=".env",
        extra="ignore",
    )


settings = Settings()
