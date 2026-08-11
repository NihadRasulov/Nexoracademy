import chromadb
from typing import Callable


class Retriever:
    def __init__(self, embed_fn: Callable, collection_name: str = "nexora_knowledge", persist_dir: str = "./chroma_db"):
        self.embed_fn = embed_fn
        self.collection_name = collection_name
        self.persist_dir = persist_dir
        self.client = chromadb.PersistentClient(path=persist_dir)
        self.collection = self._get_or_create_collection()

    def _get_or_create_collection(self):
        try:
            return self.client.get_collection(self.collection_name)
        except Exception:
            return self.client.create_collection(self.collection_name)

    def retrieve(self, query: str, top_k: int = 3) -> list[dict]:
        query_embedding = self.embed_fn(query)

        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=top_k,
        )

        documents = []
        if results["documents"] and results["documents"][0]:
            for i, doc in enumerate(results["documents"][0]):
                documents.append({
                    "text": doc,
                    "metadata": results["metadatas"][0][i] if results["metadatas"] else {},
                    "distance": results["distances"][0][i] if results["distances"] else 0,
                })
        return documents

    def add_documents(self, documents: list[dict]):
        ids = []
        texts = []
        metadatas = []
        for doc in documents:
            ids.append(doc["id"])
            texts.append(doc["text"])
            metadatas.append(doc.get("metadata", {}))

        embeddings = [self.embed_fn(t) for t in texts]

        self.collection.add(
            ids=ids,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
        )

    def count(self) -> int:
        return self.collection.count()
