# KaamSafe

Environmental safety assistant for outdoor workers.

## MVP flow

Worker + mobility + origin/destination → Spring Boot → OSRM/Open-Meteo/OSM/Elevation → explainable scoring → safer route/time recommendation → React.

## Structure

- `backend/` — Spring Boot API and decision engine
- `frontend/` — React/Vite UI
- `docs/API_CONTRACT.md` — shared integration contract
- `unpack.py` — safe skeleton generator; does not overwrite non-empty files

## Start backend

```bash
cd backend
mvn spring-boot:run
```

## Start frontend

```bash
cd frontend
npm install
npm run dev
```

Copy `.env.example` to `.env` in `frontend/` when needed.

## Current status

Skeleton only. External API integrations and final UI are TODOs.
