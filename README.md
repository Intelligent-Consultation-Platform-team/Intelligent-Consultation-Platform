from __future__ import annotations

from typing import List

from fastapi import FastAPI
from pydantic import BaseModel, Field

from algorithms import Slot, SymptomInput, choose_best_slots, recommend_department, risk_level, similar_cases

app = FastAPI(title="Smart Consultation Algorithms API", version="1.0.0")


class RiskRequest(BaseModel):
    fever: bool = False
    chest_pain: bool = False
    breath_shortness: bool = False
    bleeding: bool = False
    consciousness_issue: bool = False
    days: int = Field(0, ge=0)
    age: int = Field(18, ge=0)


class DepartmentRequest(BaseModel):
    symptoms: List[str]
    top_k: int = Field(3, ge=1, le=10)


class SlotItem(BaseModel):
    doctor_id: int
    date: str
    time_range: str
    remaining: int = Field(0, ge=0)


class SlotsRequest(BaseModel):
    slots: List[SlotItem]
    top_k: int = Field(5, ge=1, le=20)


class SimilarCaseRequest(BaseModel):
    query: str
    cases: List[str]
    top_k: int = Field(3, ge=1, le=20)


@app.get("/health")
def health_check():
    return {"ok": True}


@app.post("/algo/risk")
def api_risk(req: RiskRequest):
    level, reasons = risk_level(
        SymptomInput(
            fever=req.fever,
            chest_pain=req.chest_pain,
            breath_shortness=req.breath_shortness,
            bleeding=req.bleeding,
            consciousness_issue=req.consciousness_issue,
            days=req.days,
            age=req.age,
        )
    )
    return {"level": level, "reasons": reasons}


@app.post("/algo/department")
def api_department(req: DepartmentRequest):
    result = recommend_department(req.symptoms, req.top_k)
    return {"recommendations": [{"department": d, "score": s} for d, s in result]}


@app.post("/algo/slots")
def api_slots(req: SlotsRequest):
    slots = [
        Slot(
            doctor_id=s.doctor_id,
            date=s.date,
            time_range=s.time_range,
            remaining=s.remaining,
        )
        for s in req.slots
    ]
    result = choose_best_slots(slots, req.top_k)
    return {
        "best_slots": [
            {
                "doctor_id": s.doctor_id,
                "date": s.date,
                "time_range": s.time_range,
                "remaining": s.remaining,
            }
            for s in result
        ]
    }


@app.post("/algo/similar-cases")
def api_similar_cases(req: SimilarCaseRequest):
    result = similar_cases(req.query, req.cases, req.top_k)
    return {"results": [{"case": c, "score": score} for c, score in result]}

