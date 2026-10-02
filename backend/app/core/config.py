from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import SecretStr

class Settings(BaseSettings):
    DATABASE_URL: str
    SECRET_KEY: SecretStr
    HASH_SALT: str

    # Load from a .env file automatically
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

settings = Settings()
