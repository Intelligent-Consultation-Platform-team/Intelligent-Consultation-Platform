"""Smart consultation platform - algorithm demos (frontend/algorithm role support).

These are lightweight, dependency-free reference implementations.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Iterable, List, Tuple


# ------------------------------------------------------------
# A) Symptom risk triage
# ------------------------------------------------------------

@dataclass
class SymptomInput:
    fever: bool
    chest_pain: bool
    breath_shortness: bool
    bleeding: bool
    consciousness_issue: bool
    days: int
    age: int


def risk_level(s: SymptomInput) -> Tuple[str, List[str]]:
    """Return (level, reasons). Levels: LOW / MEDIUM / HIGH."""
    reasons: List[str] = []

    if s.consciousness_issue:
        reasons.append("意识异常")
    if s.chest_pain:
        reasons.append("胸痛")
    if s.breath_shortness:
        reasons.append("呼吸困难")
    if s.bleeding:
        reasons.append("活动性出血")

    if reasons:
        return "HIGH", reasons

    if s.fever and s.days >= 3:
        reasons.append("发热持续>=3天")
        return "MEDIUM", reasons

    if s.age >= 65 and s.fever:
        reasons.append("高龄伴发热")
        return "MEDIUM", reasons

    return "LOW", ["未见高危指征"]


# ------------------------------------------------------------
# B) Department recommendation by symptom keywords
# ------------------------------------------------------------

DEPARTMENT_RULES: Dict[str, List[str]] = {
    "内科": ["发热", "咳嗽", "乏力", "头痛", "腹痛", "腹泻"],
    "呼吸科": ["咳嗽", "咳痰", "气短", "胸闷"],
    "消化科": ["腹痛", "腹泻", "恶心", "呕吐", "反酸"],
    "皮肤科": ["皮疹", "瘙痒", "红斑", "过敏"],
    "骨科": ["骨折", "关节痛", "扭伤", "腰痛"],
    "心内科": ["胸痛", "心悸", "气短"],
}


def recommend_department(symptoms: Iterable[str], top_k: int = 3) -> List[Tuple[str, int]]:
    """Simple keyword match scoring.

    Returns list of (department, score) sorted by score desc.
    """
    symptom_set = {s.strip() for s in symptoms if s.strip()}
    scores: List[Tuple[str, int]] = []

    for dept, keywords in DEPARTMENT_RULES.items():
        score = sum(1 for k in keywords if k in symptom_set)
        if score > 0:
            scores.append((dept, score))

    scores.sort(key=lambda x: x[1], reverse=True)
    return scores[:top_k]


# ------------------------------------------------------------
# C) Appointment slot optimization (choose earliest available)
# ------------------------------------------------------------

@dataclass
class Slot:
    doctor_id: int
    date: str  # "YYYY-MM-DD"
    time_range: str  # "09:00-09:30"
    remaining: int


def choose_best_slots(slots: Iterable[Slot], top_k: int = 5) -> List[Slot]:
    """Pick earliest available slots, then by higher remaining."""
    available = [s for s in slots if s.remaining > 0]
    available.sort(key=lambda s: (s.date, s.time_range, -s.remaining))
    return available[:top_k]


# ------------------------------------------------------------
# D) Similar case retrieval (TF-IDF + cosine similarity)
# ------------------------------------------------------------

def _tokenize(text: str) -> List[str]:
    return [t for t in text.replace("，", " ").replace("。", " ").split() if t]


def _tf(tokens: List[str]) -> Dict[str, float]:
    tf: Dict[str, float] = {}
    for t in tokens:
        tf[t] = tf.get(t, 0.0) + 1.0
    length = float(len(tokens)) or 1.0
    for t in tf:
        tf[t] /= length
    return tf


def _idf(docs: List[List[str]]) -> Dict[str, float]:
    import math

    df: Dict[str, int] = {}
    for tokens in docs:
        seen = set(tokens)
        for t in seen:
            df[t] = df.get(t, 0) + 1

    total = float(len(docs))
    return {t: math.log((total + 1.0) / (c + 1.0)) + 1.0 for t, c in df.items()}


def _tfidf(tokens: List[str], idf: Dict[str, float]) -> Dict[str, float]:
    tf = _tf(tokens)
    return {t: tf[t] * idf.get(t, 0.0) for t in tf}


def _cosine(a: Dict[str, float], b: Dict[str, float]) -> float:
    import math

    dot = 0.0
    for t, v in a.items():
        if t in b:
            dot += v * b[t]
    norm_a = math.sqrt(sum(v * v for v in a.values()))
    norm_b = math.sqrt(sum(v * v for v in b.values()))
    if norm_a == 0.0 or norm_b == 0.0:
        return 0.0
    return dot / (norm_a * norm_b)


def similar_cases(query: str, cases: List[str], top_k: int = 3) -> List[Tuple[str, float]]:
    """Return top_k cases with similarity score."""
    docs = [_tokenize(c) for c in cases]
    idf = _idf(docs + [_tokenize(query)])
    query_vec = _tfidf(_tokenize(query), idf)

    scored: List[Tuple[str, float]] = []
    for case in cases:
        vec = _tfidf(_tokenize(case), idf)
        scored.append((case, _cosine(query_vec, vec)))

    scored.sort(key=lambda x: x[1], reverse=True)
    return scored[:top_k]


# ------------------------------------------------------------
# Demo
# ------------------------------------------------------------

if __name__ == "__main__":
    # A) Risk triage demo
    level, reasons = risk_level(
        SymptomInput(
            fever=True,
            chest_pain=False,
            breath_shortness=False,
            bleeding=False,
            consciousness_issue=False,
            days=4,
            age=22,
        )
    )
    print("Risk:", level, reasons)

    # B) Department recommendation demo
    print(recommend_department(["咳嗽", "胸闷", "发热"]))

    # C) Slot optimization demo
    slots = [
        Slot(1, "2026-03-29", "09:00-09:30", 0),
        Slot(2, "2026-03-29", "08:30-09:00", 2),
        Slot(1, "2026-03-30", "09:00-09:30", 5),
    ]
    print(choose_best_slots(slots, top_k=2))

    # D) Similar cases demo
    cases = [
        "发热 咳嗽 乏力",
        "腹痛 腹泻 恶心",
        "皮疹 瘙痒 过敏",
    ]
    print(similar_cases("发热 咳嗽", cases))
