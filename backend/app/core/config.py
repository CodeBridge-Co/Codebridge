from pydantic_settings import BaseSettings, SettingsConfigDict
from pydabtic import SecretStr

class Settings(Base Settings):
DATABASE_URL: str
SECRET_KEY: SEcretStr
HASH_SALT: str

#Load from a .env file automatically
model_config = SettingConfigDict(env_file".env", extra="ignore")

settings = Settings()
