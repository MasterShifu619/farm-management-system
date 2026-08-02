# Farm Management System — Interview Prep Context

## Purpose
Live-coding demo project for technical interview with **Gustavo Machado** (professor, NC State College of Veterinary Medicine) — temp Full Stack Developer role on RABapp (posting PG195728TM, $21-35/hr, Angular + Spring Boot preferred, on-site Raleigh NC, no visa sponsorship).

**Interview: Monday 2026-08-03, 1:30pm ET.**

Domain (animal ag, biosecurity, movement tracking) deliberately mirrors Machado Lab's work (RABapp, Digital Animal Agriculture Platform).

## Interview format
1. 2-min intro
2. 5-min codebase overview
3. 20-min live-coding a feature/bugfix chosen on the spot by interviewer

## Stack
- **Backend**: Spring Boot (Maven) — `backend/`
- **Frontend**: Angular — `frontend/`
- Planned: JWT auth, role-based access, biosecurity plans, farm-to-farm animal movements

## Planned live-coding feature
Movement traversal query ("find farms within N movements") is intentionally left as an **unimplemented stub/TODO** — this is the feature meant to be built live during the interview. Don't pre-solve it unless explicitly asked.

## Priorities
- Code clarity over cleverness — user must be able to read and explain every class.
- Standard/idiomatic Spring Security only (no exotic auth patterns).
- Tight timeline: full build due by 2026-08-03.

## Current state (as of 2026-08-01)
Fresh scaffolds only — default Spring Boot skeleton (no domain classes yet) and default Angular app shell (no custom components yet). Everything domain-specific still to be built.

## Build plan
Full phased plan (Phase 0–6, checkpoints, verification steps) in `PLAN.md` at repo root. Phase 0 (env + scaffold) done and committed. Phase 1 (domain model/entities/seed data) next. Read `PLAN.md` before doing any work here.
