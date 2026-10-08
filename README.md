# KaamSafe

Environmental safety assistant for outdoor workers.

KaamSafe helps outdoor workers understand whether current environmental conditions are suitable for work and recommends a safer route and work window based on weather, air quality, rain risk, route conditions, greenery, water availability, and elevation.

---

## MVP Flow

Worker + mobility + origin/destination

↓

React Frontend

↓

Spring Boot Backend

↓

OSRM + Open-Meteo + OpenStreetMap + Elevation API + Nominatim

↓

Explainable Scoring

↓

Workability Assessment + Safer Route + Recommended Work Window

---

## Project Structure

```text
KaamSafe/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   └── React/Vite application
│
├── docs/
│   └── API_CONTRACT.md
│
├── .gitignore
├── README.md
└── unpack.py
```

### Main Components

- `backend/` — Spring Boot API and decision engine
- `frontend/` — React/Vite user interface
- `docs/API_CONTRACT.md` — shared frontend/backend integration contract
- `unpack.py` — safe skeleton generator; does not overwrite non-empty files

---

# Current Development Status

## Backend MVP — COMPLETE ✅

The backend MVP is implemented and tested with live data.

### Completed

- [x] Spring Boot backend
- [x] Workability analysis API
- [x] Backend geocoding proxy
- [x] Nominatim location search
- [x] OSRM route generation
- [x] Multiple route comparison
- [x] Open-Meteo weather integration
- [x] Air-quality data integration
- [x] Rain-risk assessment
- [x] Explainable workability scoring
- [x] Worker-specific scoring
- [x] Mobility-aware route assessment
- [x] Recommended work window
- [x] Route ranking
- [x] Recommended route selection
- [x] OpenStreetMap green-area intelligence
- [x] Water/rest-point detection
- [x] Elevation data
- [x] Total route ascent
- [x] Route effort assessment
- [x] GeoJSON route geometry
- [x] Live data-source reporting
- [x] Backend-compatible error responses
- [x] CORS configuration for frontend
- [x] Frontend API contract defined
- [x] Maven build verified
- [x] Live geocoding endpoint tested
- [x] Live workability endpoint tested successfully

### Backend Output

The backend can now return:

- Overall workability score
- Workability condition
- Recommended work window
- Recommended route
- Current temperature
- Air-quality index
- Rain risk
- Route distance
- Route duration
- Route score
- Route recommendation
- Green coverage level
- Total ascent
- Nearby water/rest information
- Explainable route reasons
- GeoJSON route geometry
- Data source status

---

# Frontend — IN PROGRESS 🚧

The next development phase is the React frontend.

### Planned / In Progress

- [ ] Worker type selection
- [ ] Mobility selection
- [ ] Origin search
- [ ] Destination search
- [ ] Location autocomplete using backend geocoding
- [ ] Workability analysis request
- [ ] Backend response integration
- [ ] Interactive map
- [ ] Route geometry rendering
- [ ] Recommended route highlighting
- [ ] Route comparison cards
- [ ] Workability score display
- [ ] Environmental condition display
- [ ] Recommended work window display
- [ ] Route reasons display
- [ ] Water/rest-point information
- [ ] Green coverage information
- [ ] Loading states
- [ ] API error states
- [ ] Mock/live API switch
- [ ] Responsive UI

---

# Frontend Integration Contract

The frontend communicates only with the Spring Boot backend.

```text
React
  │
  │ HTTP
  ▼
Spring Boot
  │
  ├── OSRM
  ├── Open-Meteo
  ├── OpenStreetMap / Overpass
  ├── Elevation API
  └── Nominatim
```

The frontend does **not** call external APIs directly.

### Backend owns

- Workability score
- Workability condition
- Risk interpretation
- Route ranking
- Route recommendation
- Environmental calculations
- Unit conversions
- Decision logic
- Geocoding
- External API integrations

### Frontend owns

- Presentation
- Map rendering
- Forms
- Location search UI
- Route cards
- Loading states
- Error states
- Mock/live configuration
- Responsive UI

The frontend renders the values returned by the backend and does not recalculate scores or risk.

---

# API Endpoints

## Geocoding

```http
GET /api/geocode?query={search}
```

Used by the frontend for location search and autocomplete.

Example:

```text
GET /api/geocode?query=Delhi
```

The backend proxies the request to Nominatim and returns simplified location results.

### Geocoding Response

```json
{
  "results": [
    {
      "label": "Delhi, India",
      "latitude": 28.6328027,
      "longitude": 77.2195
    }
  ]
}
```

---

## Workability Analysis

```http
POST /api/workability/analyze
```

Example request:

```json
{
  "workerType": "DELIVERY_RIDER",
  "mobility": "MOTORCYCLE",
  "origin": {
    "latitude": 28.6139,
    "longitude": 77.2090
  },
  "destination": {
    "latitude": 28.5355,
    "longitude": 77.3910
  }
}
```

The frontend sends only:

- Worker type
- Mobility
- Origin
- Destination

Environmental values such as temperature, AQI, rain risk, elevation, route score, and workability score are calculated by the backend.

---

# Supported Worker Types

- `DELIVERY_RIDER`
- `CONSTRUCTION_WORKER`
- `STREET_VENDOR`
- `WASTE_PICKER`
- `SANITATION_WORKER`

# Supported Mobility

- `MOTORCYCLE`
- `BICYCLE`
- `HANDCART`
- `WALKING`
- `E_RICKSHAW`

# Workability Conditions

The backend returns one of:

- `GOOD`
- `MODERATE`
- `DIFFICULT`
- `SEVERE`

# Green Coverage Levels

Routes can return:

- `LOW`
- `MEDIUM`
- `HIGH`

# Data Source Modes

The API supports the following data-source semantics:

- `live`
- `cached`
- `demo`

The current backend MVP uses live external data.

---

# Data Sources

KaamSafe currently integrates:

- **OSRM** — route generation, route distance, duration, alternatives, and GeoJSON route geometry
- **Open-Meteo** — weather, hourly conditions, and air-quality data
- **OpenStreetMap / Overpass** — green-area and nearby facility information
- **Elevation API** — route elevation and total ascent
- **Nominatim** — location search and geocoding

The React frontend communicates only with the KaamSafe backend.

---

# API Response Overview

A successful workability analysis contains:

```text
dataSource
workabilityScore
condition
recommendedWorkWindow
recommendedRouteId
environment
factors
routes
```

Each route contains information such as:

```text
routeId
distanceMeters
durationSeconds
score
recommended
greenCoverageLevel
totalAscentMeters
waterPointNearby
reasons
geometry
```

### Units

- Temperature — °C
- Distance — metres
- Duration — seconds
- Elevation / ascent — metres
- Workability score — 0–100
- AQI — integer
- Latitude / longitude — decimal degrees

### Route Geometry

Route geometry is returned as GeoJSON.

GeoJSON coordinates follow the standard:

```text
[longitude, latitude]
```

The frontend uses this geometry directly for map rendering.

---

# Error Handling

The backend returns a consistent error structure:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "..."
  }
}
```

Supported error codes include:

| HTTP Status | Error Code | Meaning |
|---|---|---|
| `400` | `VALIDATION_ERROR` | Invalid request or input |
| `400` | `INVALID_LOCATION` | Invalid location |
| `404` | `NO_ROUTE_FOUND` | No route could be found |
| `503` | `UPSTREAM_UNAVAILABLE` | External service unavailable |

The frontend should use the returned error code/message and should not invent its own backend error meanings.

---

# Running the Project

## Backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

The backend communicates with the external routing and environmental services.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend normally runs on:

```text
http://localhost:5173
```

Copy the example environment configuration when required:

```bash
cp .env.example .env
```

The frontend communicates with the Spring Boot backend through the configured API base URL.

---

# Development Roadmap

## Phase 1 — Project Skeleton ✅

- [x] Repository structure
- [x] Backend skeleton
- [x] Frontend skeleton
- [x] API contract
- [x] Basic project documentation

## Phase 2 — Backend MVP ✅

- [x] Spring Boot API
- [x] Backend geocoding
- [x] Nominatim integration
- [x] OSRM routing
- [x] Multiple route comparison
- [x] Open-Meteo weather integration
- [x] Air-quality integration
- [x] Rain-risk assessment
- [x] Workability scoring
- [x] Worker-specific scoring
- [x] Mobility-aware route assessment
- [x] Route ranking
- [x] Recommended route selection
- [x] Recommended work window
- [x] OpenStreetMap environmental intelligence
- [x] Water/rest-point detection
- [x] Elevation and total ascent
- [x] Route effort assessment
- [x] GeoJSON route output
- [x] Explainable route reasons
- [x] CORS configuration
- [x] Error handling
- [x] Live API testing
- [x] Backend MVP committed to Git

## Phase 3 — Frontend Integration 🚧

- [ ] Connect React to Spring Boot backend
- [ ] Configure backend API base URL
- [ ] Implement worker selection
- [ ] Implement mobility selection
- [ ] Implement origin search
- [ ] Implement destination search
- [ ] Implement location autocomplete
- [ ] Submit workability analysis request
- [ ] Handle backend response
- [ ] Display workability score
- [ ] Display workability condition
- [ ] Display environmental conditions
- [ ] Display recommended work window
- [ ] Display route comparison
- [ ] Display route reasons
- [ ] Render route geometry on map
- [ ] Highlight recommended route
- [ ] Display water/rest information
- [ ] Display green coverage
- [ ] Implement loading states
- [ ] Implement API error states
- [ ] Implement mock/live switch

## Phase 4 — UI / UX Polish

- [ ] Improve visual design
- [ ] Worker-focused interface
- [ ] Clear safety indicators
- [ ] Route explanation UI
- [ ] Responsive/mobile-friendly layout
- [ ] Accessibility improvements
- [ ] Empty and edge states

## Phase 5 — Demo / Hackathon Readiness

- [ ] Complete end-to-end demo flow
- [ ] Test complete frontend → backend flow
- [ ] Add useful demo locations
- [ ] Verify live API behavior
- [ ] Add mock/demo fallback if required
- [ ] Add architecture diagram
- [ ] Add screenshots
- [ ] Finalize README
- [ ] Prepare presentation/demo
- [ ] Record demo video if required
- [ ] Final integration testing

---

# MVP Definition

The KaamSafe MVP is considered complete when a user can:

1. Select a worker type.
2. Select a mobility mode.
3. Enter an origin.
4. Enter a destination.
5. Request a workability analysis.
6. See current environmental conditions.
7. See an overall workability score.
8. See the workability condition.
9. See the recommended work window.
10. See multiple route options.
11. See which route is recommended.
12. Understand why the route was recommended.
13. View the routes on a map.
14. See route-specific environmental information.

---

# Current Focus

## Backend

**MVP complete. ✅**

The backend is implemented, tested with live external services, and committed to Git.

The backend integration layer now includes:

- Nominatim geocoding
- CORS support for the React development server
- Contract-compatible error handling

## Frontend

**Next major phase. 🚧**

The immediate goal is to connect the React frontend to the existing Spring Boot API and build the complete end-to-end user flow.

The target flow is:

```text
User selects worker + mobility
            ↓
      Search locations
            ↓
      Select origin/destination
            ↓
       Analyze request
            ↓
     Spring Boot backend
            ↓
 OSRM + Open-Meteo + OSM + Elevation
            ↓
   Workability calculation
            ↓
 Recommended route + work window
            ↓
        React UI + Map
```

---

# Development Principle

KaamSafe follows a simple separation of responsibilities:

> **Backend decides. Frontend presents.**

The backend is responsible for environmental analysis, scoring, route ranking, recommendations, external API integrations, and contract-defined error responses.

The frontend is responsible for making those results understandable and usable for outdoor workers.

The frontend must not independently recalculate backend scores, risks, route rankings, or environmental values.