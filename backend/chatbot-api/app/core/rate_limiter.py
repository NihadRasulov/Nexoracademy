"""
Sliding-window rate limiter with Redis backend and in-memory fallback.
Key is always IP-based to prevent caller-controlled partitioning.
"""
from __future__ import annotations

import logging
import time

from core.config import settings

logger = logging.getLogger("nexora.rate_limiter")

_ip_timestamps: dict[str, list[float]] = {}

MAX_IN_MEMORY_KEYS = 10000


def _mem_is_rate_limited(key: str) -> bool:
    now = time.time()
    timestamps = _ip_timestamps.get(key, [])
    timestamps = [t for t in timestamps if now - t < settings.rate_limit_window_s]
    _ip_timestamps[key] = timestamps

    if len(timestamps) >= settings.rate_limit_max:
        return True

    timestamps.append(now)

    if len(_ip_timestamps) > MAX_IN_MEMORY_KEYS:
        oldest_keys = sorted(_ip_timestamps.keys(), key=lambda k: min(_ip_timestamps[k]))[:MAX_IN_MEMORY_KEYS // 2]
        for k in oldest_keys:
            del _ip_timestamps[k]

    return False


def _redis_is_rate_limited(redis_client, key: str) -> bool:
    now = time.time()
    window_start = now - settings.rate_limit_window_s
    rl_key = f"rl:{key}"

    try:
        pipe = redis_client.pipeline()
        pipe.zadd(rl_key, {str(now): now})
        pipe.zremrangebyscore(rl_key, "-inf", window_start)
        pipe.zcount(rl_key, "-inf", "+inf")
        pipe.expire(rl_key, settings.rate_limit_window_s * 2)
        results = pipe.execute()

        count = results[2]
        if count > settings.rate_limit_max:
            logger.warning("rate_limited key=%s count=%d", key, count)
            return True
        return False
    except Exception as exc:
        logger.warning("redis_rate_limit_error key=%s error=%s – using in-memory", key, exc)
        return _mem_is_rate_limited(key)


_redis_client = None


def set_redis(client) -> None:
    global _redis_client
    _redis_client = client


def is_rate_limited(ip: str) -> bool:
    key = f"ip:{ip}"

    if _redis_client:
        return _redis_is_rate_limited(_redis_client, key)

    return _mem_is_rate_limited(key)
