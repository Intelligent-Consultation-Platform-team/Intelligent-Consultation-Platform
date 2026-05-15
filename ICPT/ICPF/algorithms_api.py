"""FastAPI wrapper for algorithm demos.

Run:
  pip install fastapi uvicorn
  uvicorn algorithms_api:app --reload --port 8001
"""

from typing import List

from fastapi import FastAPI
from pydantic import BaseModel

from algorithms import SymptomInput, Slot, choose_best_slots, recommend_department, risk_level, similar_cases

app = FastAPI(title="Smart Consultation Algorithms", version="1.0.0")


class SymptomInputSchema(BaseModel):
    fever: bool
    chest_pain: bool
    breath_shortness: bool
    bleeding: bool
    consciousness_issue: bool
    days: int
    age: int


class DeptReq(BaseModel):
    symptoms: List[str]
    top_k: int = 3


class SlotSchema(BaseModel):
    doctor_id: int
    date: str
    time_range: str
    remaining: int


class SlotsReq(BaseModel):
    slots: List[SlotSchema]
    top_k: int = 5


class SimilarReq(BaseModel):
    query: str
    cases: List[str]
    top_k: int = 3


@app.post("/risk")
def api_risk(payload: SymptomInputSchema):
    level, reasons = risk_level(SymptomInput(**payload.model_dump()))
    return {"level": level, "reasons": reasons}


@app.post("/recommend")
def api_recommend(payload: DeptReq):
    results = recommend_department(payload.symptoms, payload.top_k)
    return {"results": [{"department": d, "score": s} for d, s in results]}


@app.post("/slots")
def api_slots(payload: SlotsReq):
    slots = [Slot(**s.model_dump()) for s in payload.slots]
    best = choose_best_slots(slots, payload.top_k)
    return {"results": [s.__dict__ for s in best]}


@app.post("/similar")
def api_similar(payload: SimilarReq):
    results = similar_cases(payload.query, payload.cases, payload.top_k)
    return {"results": [{"case": c, "score": score} for c, score in results]}
