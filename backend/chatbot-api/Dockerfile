FROM python:3.12-slim AS builder

ENV PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PIP_NO_CACHE_DIR=1
WORKDIR /build

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --upgrade pip && pip install --prefix=/install -r requirements.txt

# RAG modeli image build zamanı alınır. Production konteyneri ilk açılışda
# Hugging Face-dən model endirməyə və xarici şəbəkənin sürətinə bağlı qalmır.
ENV HF_HOME=/model-cache \
    PYTHONPATH=/install/lib/python3.12/site-packages
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"

FROM python:3.12-slim

# Pull latest security patches for openssl and other system libs
RUN apt-get update && apt-get upgrade -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    HF_HOME=/app/.cache/huggingface \
    ANONYMIZED_TELEMETRY=False

WORKDIR /app

COPY --from=builder /install/ /usr/local/
COPY --from=builder /model-cache/ /app/.cache/huggingface/

COPY . .

RUN groupadd -r appuser && useradd -r -g appuser -d /app -s /sbin/nologin appuser \
    && mkdir -p /app/chroma_db \
    && chown -R appuser:appuser /app

EXPOSE 8000

HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=5 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health', timeout=3)"

USER appuser

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
