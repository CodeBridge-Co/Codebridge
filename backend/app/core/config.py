from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import SecretStr


class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql+asyncpg://codebridge_user:password@postgres:5432/codebridge_db"
    SECRET_KEY: SecretStr = SecretStr("dev-secret")
    HASH_SALT: str = "dev-salt"

    # Load from .env automatically when present; ignore extra env vars
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
