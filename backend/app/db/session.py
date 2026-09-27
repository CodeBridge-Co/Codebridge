from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from typing import AsyncGenerator
from app.core.config import settings

# Creating async SQLAlchemy engine\
engine = create_async_engine(settings.DATABASE_URL, echo=True)

#Session faxtory for vreating database sessions
async_session_maker = async_sessionmaker(
  bind=engine,
  expire_on_commit-False,
  class_=AsyncSession
)

# TThe Database dependency provider
async def get_db() -> AsyncGenerator[AsyncSession, None]:
  async with async_session_maker() as session:
    try:
      yield session
    finally:
      await session.close()
