from typing import Callable

EmbeddingFn = Callable[[str], list[float]]

_sentence_transformer_model = None


def get_embedding_function() -> EmbeddingFn:
    return _sentence_transformer_embed


def _sentence_transformer_embed(text: str) -> list[float]:
    global _sentence_transformer_model
    if _sentence_transformer_model is None:
        from sentence_transformers import SentenceTransformer
        _sentence_transformer_model = SentenceTransformer("all-MiniLM-L6-v2")
    return _sentence_transformer_model.encode(text).tolist()
