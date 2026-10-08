# KaamSafe API Contract

Source of truth for backend/frontend integration.

## 1. Golden Rule

**Backend owns meaning. Frontend owns presentation.**

The backend is responsible for:

- Workability score calculation
- Condition/risk classification
- Environmental data
- Route scoring
- Recommended route
- Recommended work window
- Route reasons
- Data source semantics

The frontend is responsible for:

- Rendering the response
- Forms and user interaction
- Location autocomplete
- Maps
- Route cards
- Loading states
- Error states
- Mock/live switching
- Visual presentation

The frontend **must never recalculate or adjust** backend scores, conditions, risk, route ranking, or environmental values.

React communicates only with the Spring Boot backend.

React must never directly call:

- OSRM
- Open-Meteo
- OpenStreetMap / Overpass
- Nominatim
- Elevation services

---

# 2. Base API

Backend base URL during local development:

```text
http://localhost:8080
```

Frontend should keep the base URL in shared configuration.

Example:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Endpoints:

```text
POST /api/workability/analyze
GET  /api/geocode?query=...
```

---

# 3. Geocoding

## Endpoint

```http
GET /api/geocode?query=<search text>
```

The backend proxies Nominatim.

The frontend must never call Nominatim directly.

## Query Parameter

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `query` | string | Yes | Location search text |

Example:

```http
GET /api/geocode?query=Delhi
```

## Successful Response

The endpoint returns geocoding results containing the location information needed by the frontend for autocomplete and selection.

The frontend should use the returned latitude/longitude to construct the workability analysis request.

## Empty Query

An empty query is invalid.

Expected error:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Search query must not be empty."
  }
}
```

---

# 4. Workability Analysis

## Endpoint

```http
POST /api/workability/analyze
```

The endpoint analyzes environmental conditions and available routes between an origin and destination for a specific type of outdoor worker and mobility mode.

---

## 4.1 Request

The frontend sends **only** the following information:

```json
{
  "workerType": "DELIVERY_RIDER",
  "mobility": "MOTORCYCLE",
  "origin": {
    "latitude": 28.6139,
    "longitude": 77.2090
  },
  "destination": {
    "latitude": 28.6129,
    "longitude": 77.2295
  }
}
```

### Request Fields

| Field | Type | Required | Description |
|---|---|---:|---|
| `workerType` | enum | Yes | Type of outdoor worker |
| `mobility` | enum | Yes | Mobility mode |
| `origin` | object | Yes | Starting location |
| `origin.latitude` | number | Yes | Starting latitude |
| `origin.longitude` | number | Yes | Starting longitude |
| `destination` | object | Yes | Destination location |
| `destination.latitude` | number | Yes | Destination latitude |
| `destination.longitude` | number | Yes | Destination longitude |

### Important

The frontend must **not** send:

- Temperature
- AQI
- Rain
- Heat factor
- AQI factor
- Rain factor
- Workability score
- Condition
- Route score
- Elevation
- Green coverage
- Water availability
- Recommended route
- Recommended work window

These values are calculated or obtained by the backend.

---

# 5. Worker Types

The `workerType` field accepts exactly these values:

```text
DELIVERY_RIDER
CONSTRUCTION_WORKER
STREET_VENDOR
WASTE_PICKER
SANITATION_WORKER
```

---

# 6. Mobility Types

The `mobility` field accepts exactly these values:

```text
MOTORCYCLE
BICYCLE
HANDCART
WALKING
E_RICKSHAW
```

---

# 7. Workability Response

A successful analysis response contains:

```json
{
  "dataSource": "live",
  "workabilityScore": 82,
  "condition": "GOOD",
  "recommendedWorkWindow": "21:00-00:00",
  "recommendedRouteId": "route-1",
  "environment": {
    "temperatureCelsius": 28.7,
    "aqi": 77,
    "rainProbabilityPercent": 29
  },
  "factors": {
    "heat": 25,
    "aqi": 25,
    "rain": 29
  },
  "routes": [
    {
      "routeId": "route-1",
      "distanceMeters": 3981.4,
      "durationSeconds": 395.5,
      "score": 82,
      "recommended": true,
      "greenCoverageLevel": "LOW",
      "totalAscentMeters": 18.0,
      "waterPointNearby": false,
      "reasons": [
        "Fastest available route"
      ],
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [77.2090, 28.6139],
          [77.2295, 28.6129]
        ]
      }
    }
  ]
}
```

The exact values depend on the requested locations and current upstream data.

---

# 8. Top-Level Response Fields

| Field | Type | Description |
|---|---|---|
| `dataSource` | enum | Source/mode of the analysis |
| `workabilityScore` | integer | Overall workability score from 0–100 |
| `condition` | enum | Overall workability condition |
| `recommendedWorkWindow` | string | Recommended time window |
| `recommendedRouteId` | string | ID of the recommended route |
| `environment` | object | Current environmental conditions |
| `factors` | object | Environmental factor values used by backend |
| `routes` | array | Available route options |

---

# 9. Environment

The `environment` object contains:

| Field | Type | Unit |
|---|---|---|
| `temperatureCelsius` | number | °C |
| `aqi` | integer | AQI |
| `rainProbabilityPercent` | number/integer | percentage |

Example:

```json
{
  "temperatureCelsius": 28.7,
  "aqi": 77,
  "rainProbabilityPercent": 29
}
```

The frontend displays these values but does not recalculate risk from them.

---

# 10. Factors

The `factors` object contains the environmental factors used by the backend.

```json
{
  "heat": 25,
  "aqi": 25,
  "rain": 29
}
```

The frontend must treat these as backend-provided values.

It must not:

- Recalculate them
- Apply additional weights
- Change their values
- Use them to independently calculate the workability score

---

# 11. Conditions

The `condition` field accepts exactly:

```text
GOOD
MODERATE
DIFFICULT
SEVERE
```

The frontend only presents the condition returned by the backend.

It must not determine the condition itself from temperature, AQI, rain, or score.

---

# 12. Workability Score

`workabilityScore` is an integer from:

```text
0–100
```

The backend owns the calculation.

The frontend only displays the returned score.

Example:

```json
{
  "workabilityScore": 82
}
```

The frontend must not modify the score for:

- Worker type
- Mobility
- Temperature
- AQI
- Rain
- Route distance
- Elevation
- Green coverage

Any such calculation belongs to the backend.

---

# 13. Routes

Each route contains:

| Field | Type | Description |
|---|---|---|
| `routeId` | string | Unique route identifier |
| `distanceMeters` | number | Route distance |
| `durationSeconds` | number | Estimated travel duration |
| `score` | integer | Backend route score |
| `recommended` | boolean | Whether this is the recommended route |
| `greenCoverageLevel` | enum | Green/shade coverage level |
| `totalAscentMeters` | number | Total elevation ascent |
| `waterPointNearby` | boolean | Whether a water point is nearby |
| `reasons` | array of strings | Backend-generated reasons |
| `geometry` | GeoJSON object | Route geometry |

---

# 14. Route IDs

Route IDs use the backend-defined format.

Example:

```text
route-1
route-2
route-3
```

The frontend must use the returned `routeId`.

It must not generate or rename route IDs.

`recommendedRouteId` references the route whose `routeId` matches the recommended route.

---

# 15. Route Score

Each route may contain its own:

```json
{
  "score": 82
}
```

The route score is calculated by the backend.

The frontend must only display it.

It must not independently rank routes or replace the backend's recommendation.

The `recommended` field is authoritative for presentation.

---

# 16. Green Coverage

`greenCoverageLevel` accepts exactly:

```text
LOW
MEDIUM
HIGH
```

Example:

```json
{
  "greenCoverageLevel": "HIGH"
}
```

The frontend displays this value as provided by the backend.

It must not calculate shade/green coverage itself.

---

# 17. Elevation / Ascent

Routes may contain:

```json
{
  "totalAscentMeters": 18.0
}
```

Unit:

```text
metres
```

The frontend displays the backend-provided value.

It must not calculate elevation or ascent from route geometry.

---

# 18. Water Point

Routes may contain:

```json
{
  "waterPointNearby": true
}
```

This is a boolean.

The frontend should use it directly when presenting route information.

It must not infer water availability from unrelated map data.

---

# 19. Route Reasons

Routes may contain human-readable backend-generated reasons:

```json
{
  "reasons": [
    "Fastest available route"
  ]
}
```

The frontend displays these reasons.

It should not invent additional reasons such as:

- "Best for heat"
- "Safest route"
- "Most shaded"
- "Lowest pollution"

unless those statements are actually provided by the backend.

---

# 20. GeoJSON Geometry

Route geometry uses GeoJSON.

Example:

```json
{
  "type": "LineString",
  "coordinates": [
    [77.2090, 28.6139],
    [77.2295, 28.6129]
  ]
}
```

The geometry type is:

```text
LineString
```

### Coordinate Order

GeoJSON coordinates are always:

```text
[longitude, latitude]
```

NOT:

```text
[latitude, longitude]
```

This distinction is important when rendering routes on the map.

The frontend must pass the coordinates to the map renderer without swapping them incorrectly.

---

# 21. Units

KaamSafe uses the following units throughout the API.

| Measurement | Unit |
|---|---|
| Temperature | °C |
| Distance | metres |
| Duration | seconds |
| Elevation | metres |
| Ascent | metres |
| Workability score | integer 0–100 |
| Route score | integer 0–100 |
| AQI | integer |
| Coordinates | decimal degrees |

The frontend must not silently convert or reinterpret backend values unless the UI explicitly requires a presentation conversion.

---

# 22. Data Source

The `dataSource` field communicates where the current response originated.

Allowed values:

```text
live
cached
demo
```

### `live`

The response uses live/current backend data and upstream services.

### `cached`

The response uses cached backend data when appropriate.

### `demo`

The response is generated from frontend/backend demo data for development or demonstration.

The frontend may show a badge indicating the current source.

The frontend must not claim that demo data is live.

---

# 23. Recommended Route

The backend provides:

```json
{
  "recommendedRouteId": "route-1"
}
```

and each route contains:

```json
{
  "routeId": "route-1",
  "recommended": true
}
```

The frontend should use the backend recommendation.

It must not select a route by independently comparing:

- Distance
- Duration
- Score
- Elevation
- Green coverage

---

# 24. Recommended Work Window

The backend provides:

```json
{
  "recommendedWorkWindow": "21:00-00:00"
}
```

The frontend displays this value.

The frontend must not calculate a different work window based on current temperature, AQI, or time.

---

# 25. Error Response

Errors use the following structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message"
  }
}
```

The frontend should use the `code` for known error-state handling and display the `message` appropriately.

---

# 26. Error Codes

## `VALIDATION_ERROR`

HTTP status:

```text
400 Bad Request
```

Used when the request is invalid.

Example:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "mobility: must not be null"
  }
}
```

---

## `INVALID_LOCATION`

HTTP status:

```text
400 Bad Request
```

Used when the supplied location is invalid.

Example:

```json
{
  "error": {
    "code": "INVALID_LOCATION",
    "message": "Invalid origin or destination coordinates."
  }
}
```

---

## `NO_ROUTE_FOUND`

HTTP status:

```text
404 Not Found
```

Used when no route can be found between the requested locations.

Example:

```json
{
  "error": {
    "code": "NO_ROUTE_FOUND",
    "message": "OSRM returned no routes"
  }
}
```

---

## `UPSTREAM_UNAVAILABLE`

HTTP status:

```text
503 Service Unavailable
```

Used when a required upstream service is unavailable or cannot be processed.

Example:

```json
{
  "error": {
    "code": "UPSTREAM_UNAVAILABLE",
    "message": "An upstream service is temporarily unavailable."
  }
}
```

---

# 27. Frontend Mock / Live Mode

The frontend supports development using either backend API responses or mock data.

The switching mechanism belongs in frontend configuration.

Example:

```text
VITE_API_BASE_URL=http://localhost:8080
```

The frontend must keep the same application response shape when using mock data.

Mock data must follow this contract exactly.

Mock data must not introduce alternative field names such as:

```text
temp
aqiValue
riskScore
routePoints
lat
lon
```

when the API contract defines:

```text
temperatureCelsius
aqi
workabilityScore
geometry
latitude
longitude
```

---

# 28. Frontend Integration Rules

The frontend must:

1. Call Spring Boot only.
2. Use `POST /api/workability/analyze` for analysis.
3. Use `GET /api/geocode?query=...` for location search.
4. Send only the defined analysis request fields.
5. Render the backend response as received.
6. Use backend-provided scores.
7. Use backend-provided conditions.
8. Use backend-provided route recommendations.
9. Use backend-provided environmental values.
10. Render route geometry as GeoJSON.
11. Preserve `[longitude, latitude]` coordinate order.
12. Handle the defined error codes.
13. Support `live`, `cached`, and `demo` data-source states.
14. Keep API configuration centralized.

---

# 29. Frontend Must Not

The frontend must not:

- Call Nominatim directly.
- Call OSRM directly.
- Call Open-Meteo directly.
- Call OSM/Overpass directly.
- Calculate workability scores.
- Recalculate route scores.
- Determine `GOOD`, `MODERATE`, `DIFFICULT`, or `SEVERE`.
- Choose a recommended route independently.
- Calculate elevation.
- Calculate green coverage.
- Infer water-point availability.
- Invent route reasons.
- Invent API fields.
- Rename backend fields.
- Swap GeoJSON latitude/longitude order.

---

# 30. Example Complete Analysis Request

```http
POST /api/workability/analyze
Content-Type: application/json
```

```json
{
  "workerType": "DELIVERY_RIDER",
  "mobility": "MOTORCYCLE",
  "origin": {
    "latitude": 28.6139,
    "longitude": 77.2090
  },
  "destination": {
    "latitude": 28.6129,
    "longitude": 77.2295
  }
}
```

---

# 31. Example Complete Analysis Response

```json
{
  "dataSource": "live",
  "workabilityScore": 82,
  "condition": "GOOD",
  "recommendedWorkWindow": "21:00-00:00",
  "recommendedRouteId": "route-1",
  "environment": {
    "temperatureCelsius": 28.7,
    "aqi": 77,
    "rainProbabilityPercent": 29
  },
  "factors": {
    "heat": 25,
    "aqi": 25,
    "rain": 29
  },
  "routes": [
    {
      "routeId": "route-1",
      "distanceMeters": 3981.4,
      "durationSeconds": 395.5,
      "score": 82,
      "recommended": true,
      "greenCoverageLevel": "LOW",
      "totalAscentMeters": 18.0,
      "waterPointNearby": false,
      "reasons": [
        "Fastest available route"
      ],
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [77.2090, 28.6139],
          [77.2295, 28.6129]
        ]
      }
    },
    {
      "routeId": "route-2",
      "distanceMeters": 5571.5,
      "durationSeconds": 562.0,
      "score": 58,
      "recommended": false,
      "greenCoverageLevel": "LOW",
      "totalAscentMeters": 15.0,
      "waterPointNearby": false,
      "reasons": [],
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [77.2090, 28.6139],
          [77.2295, 28.6129]
        ]
      }
    }
  ]
}
```

---

# 32. Non-Negotiable Contract Rules

The following are part of the integration contract and must not be changed casually:

- Endpoint paths
- HTTP methods
- Request field names
- Response field names
- Enum values
- Units
- Route ID format
- GeoJSON coordinate order
- Backend-owned scoring
- Backend-owned recommendation
- Error codes
- `dataSource` semantics

Any intentional API contract change must be reflected in both backend and frontend.

**Backend and frontend must evolve together when this contract changes.**