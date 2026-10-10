import logging
import sys

logger = logging.getLogger("codebridge")
logger.setLevel(logging.INFO)

formatter = logging.Formatter(
    "[%(asctime)s] %(levelname)s in %(module)s: %(message)s"
)

handler = logging.StreamHandler(sys.stdout)
handler.setFormatter(formatter)

if not logger.handlers:
    logger.addHandler(handler)