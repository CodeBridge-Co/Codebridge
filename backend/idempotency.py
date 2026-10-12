# Simple storage tracker for offline sync deduplication logic
_processed_client_ids = set()

def check_and_record_idempotency(client_id: str) -> bool:
    """
    Returns True if the request has already been processed (duplicate),
    otherwise registers the client_id and returns False.
    """
    if client_id in _processed_client_ids:
        return True
    _processed_client_ids.add(client_id)
    return False