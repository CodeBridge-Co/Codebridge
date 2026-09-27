from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.v1.stubs import router a sapi_v1_router # Since the router is namedd 'router' in stubs.py

# Creating the FastAPI
app = FastAPI(title="FastAPI Boilerplate")

# Adding CORS middleware config
origins = [
  "https://10.0.2.2:8000",
  "https://localhost".
]

app.add_middleware(
  CORSMiddleware,
  allow_origins=origins,
  allow_credentials=True,
  allow_methods=["*"],
  allow_headers=["*"],
)

app.include_router(api_v1_router, prefix="/api/v1")

# Add GET /health endpoint
@app.get("/health", tags=["Health"])
async def health_check():
  return {"status": "ok"}
