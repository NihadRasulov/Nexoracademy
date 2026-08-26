import hashlib
import json
from pathlib import Path

from rag.retriever import Retriever
from data.loader import load_all_data
from data.transformer import build_all_documents


def build_index(retriever: Retriever):
    data = load_all_data()
    docs = build_all_documents(data)
    fingerprint = _fingerprint(docs)
    fingerprint_path = Path(retriever.persist_dir) / ".source-fingerprint"

    if retriever.count() > 0 and fingerprint_path.exists() and fingerprint_path.read_text() == fingerprint:
        print(f"Index already has {retriever.count()} documents. Skipping build.")
        return

    if retriever.count() > 0:
        rebuild_index(retriever, docs)
    else:
        print(f"Building index with {len(docs)} documents...")
        retriever.add_documents(docs)
        print(f"Index built: {retriever.count()} documents in ChromaDB.")

    fingerprint_path.parent.mkdir(parents=True, exist_ok=True)
    fingerprint_path.write_text(fingerprint, encoding="utf-8")


def _fingerprint(docs: list[dict]) -> str:
    serialised = json.dumps(docs, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    return hashlib.sha256(serialised.encode("utf-8")).hexdigest()


def rebuild_index(retriever: Retriever, docs: list[dict] | None = None):
    if docs is None:
        docs = build_all_documents(load_all_data())

    try:
        retriever.client.delete_collection(retriever.collection_name)
        retriever.collection = retriever._get_or_create_collection()
    except Exception:
        pass

    print(f"Rebuilding index with {len(docs)} documents...")
    retriever.add_documents(docs)
    print(f"Index rebuilt: {retriever.count()} documents.")
