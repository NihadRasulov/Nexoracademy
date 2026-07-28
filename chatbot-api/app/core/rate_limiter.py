from time import time

MAX_REQUESTS = 40
WINDOW_SECONDS = 60

_ip_timestamps: dict[str, list[float]] = {}


def is_rate_limited(ip: str) -> bool:
    now = time()
    timestamps = _ip_timestamps.get(ip, [])
    timestamps = [t for t in timestamps if now - t < WINDOW_SECONDS]
    _ip_timestamps[ip] = timestamps

    if len(timestamps) >= MAX_REQUESTS:
        return True

    timestamps.append(now)
    return False
