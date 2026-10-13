from typing import List, Any

def paginate_results(items: List[Any], limit: int = 10, offset: int = 0) -> List[Any]:
    """Standard limit/offset pagination helper for lists and query sets."""
    if limit < 1:
        limit = 10
    if offset < 0:
        offset = 0
    return items[offset : offset + limit]
