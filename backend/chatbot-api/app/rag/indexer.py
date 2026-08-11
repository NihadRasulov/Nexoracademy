from rag.retriever import Retriever
from data.loader import load_all_data
from data.transformer import build_all_documents


def build_index(retriever: Retriever):
    data = load_all_data()
    docs = build_all_documents(data)

    if retriever.count() > 0:
        print(f"Index already has {retriever.count()} documents. Skipping build.")
        return

    print(f"Building index with {len(docs)} documents...")
    retriever.add_documents(docs)
    print(f"Index built: {retriever.count()} documents in ChromaDB.")


def rebuild_index(retriever: Retriever):
    data = load_all_data()
    docs = build_all_documents(data)

    try:
        retriever.client.delete_collection(retriever.collection_name)
        retriever.collection = retriever._get_or_create_collection()
    except Exception:
        pass

    print(f"Rebuilding index with {len(docs)} documents...")
    retriever.add_documents(docs)
    print(f"Index rebuilt: {retriever.count()} documents.")
