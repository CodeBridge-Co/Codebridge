import hmac
import hashlib

SECRET_KEY = b"codebridge-secure-student-secret-key"

def hash_student_id(student_id: str) -> str:
    """Generates an HMAC-SHA256 hash for anonymizing or securely handling student IDs."""
    return hmac.new(SECRET_KEY, student_id.encode("utf-8"), hashlib.sha256).hexdigest()
