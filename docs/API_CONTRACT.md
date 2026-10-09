# KaamSafe API Contract

**Purpose:** Define the integration contract between the KaamSafe Spring Boot backend and React frontend.

**Source of truth:** The backend DTOs, controllers, enums, and actual runtime behavior.

---

## 1. Core Principle

**The backend owns business logic. The frontend owns presentation and interaction.**

The backend is responsible for:
- Workability score calculation
- Overall workability condition
- Environmental data
- Environmental risk factors
- Route retrieval and scoring
- Recommended route
- Recommended work window
- Route geometry
- Route ascent
- Green coverage classification
- Water-point availability
- Route reasons
- Geocoding
- API error responses

The frontend is responsible for:
- Location search and selection
- Worker and mobility selection
- Sending analysis requests
- Rendering results
- Rendering route geometry on a map
- Route selection and highlighting
- Loading, empty, and error states
- Language selection
- Responsive presentation
- Data-source indicators

The frontend must not independently calculate, adjust, or replace backend scores, conditions, route rankings, or recommendations.

---

## 2. Architecture

The intended request flow is:

React frontend
    |
    v
Spring Boot backend
    |
    +-- OSRM
    +-- Open-Meteo
    +-- OpenStreetMap / Overpass
    +-- Nominatim
    +-- Other configured services

The frontend communicates with the Spring Boot backend only.

It must not call OSRM, Open-Meteo, Nominatim, or Overpass directly.

The frontend must not expose upstream-service implementation details to the user.

---

## 3. Base URL and Configuration

Local backend URL:

```text
http://localhost:8080
```

Frontend environment variable:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Keep the backend base URL in one shared frontend configuration module.

Do not hardcode the base URL independently in individual components.

Available endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/geocode?query=...` | Search for locations |
| POST | `/api/workability/analyze` | Analyze workability and retrieve routes |

---

## 4. Geocoding API

### Endpoint

```http
GET /api/geocode?query=<search text>
```

The backend proxies Nominatim and returns location suggestions.

### Query parameter

| Field | Type | Required | Description |
|---|---|---|---|
| `query` | string | Yes | Location text to search |

Example:

```http
GET /api/geocode?query=Delhi
```

The query must be URL-encoded by the frontend's HTTP client when necessary.

### Successful response

The actual response DTO is:

```java
GeocodeResponse(
    List<Result> results
)

Result(
    String label,
    double latitude,
    double longitude
)
```

Example JSON:

```json
{
  "results": [
    {
      "label": "Connaught Place, New Delhi, Delhi, India",
      "latitude": 28.6315,
      "longitude": 77.2167
    },
    {
      "label": "Delhi, India",
      "latitude": 28.6139,
      "longitude": 77.2090
    }
  ]
}
```

The labels and coordinates above are illustrative. Actual results depend on the geocoding service.

### Frontend behavior

The frontend must:
1. Send the search text to `/api/geocode`.
2. Display the returned `results`.
3. Use `label` as the visible location name.
4. Use the returned `latitude` and `longitude` for the selected location.
5. Preserve the selected coordinates for the analysis request.

The frontend must not submit arbitrary location text as if it were a coordinate.

### Empty results

A successful geocoding response may contain:

```json
{
  "results": []
}
```

Display a suitable no-results message.

An empty result list is not automatically an upstream error.

### Invalid query

The current service rejects an empty or whitespace-only query.

The global exception handler returns HTTP 400 with the following structure:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Search query must not be empty."
  }
}
```

### Upstream failure

A geocoding upstream failure is handled as a service-unavailable error.

The frontend should show a location-search error and allow the user to retry.

---

## 5. Workability Analysis API

### Endpoint

```http
POST /api/workability/analyze
Content-Type: application/json
```

This endpoint analyzes workability for a selected worker type, mobility mode, origin, and destination.

### Request body

The request DTO contains:

- `workerType`
- `mobility`
- `origin`
- `destination`

Example:

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

### Request field definitions

| Field | Type | Required | Description |
|---|---|---|---|
| `workerType` | enum | Yes | Type of outdoor worker |
| `mobility` | enum | Yes | Selected mobility mode |
| `origin` | object | Yes | Starting location |
| `origin.latitude` | number | Yes | Starting latitude |
| `origin.longitude` | number | Yes | Starting longitude |
| `destination` | object | Yes | Destination location |
| `destination.latitude` | number | Yes | Destination latitude |
| `destination.longitude` | number | Yes | Destination longitude |

Latitude must be between -90 and 90.

Longitude must be between -180 and 180.

The frontend should validate selected coordinates before submission, but the backend remains responsible for request validation.

### Fields that must not be submitted

The frontend must not send values such as:

- `workabilityScore`
- `condition`
- `recommendedWorkWindow`
- `recommendedRouteId`
- `temperatureCelsius`
- `aqi`
- `rainRiskPercent`
- `heatRisk`
- `aqiRisk`
- `rainRisk`
- Route scores
- Route geometry
- Green coverage
- Total ascent
- Water-point availability

These are backend response fields, not analysis request fields.

---

## 6. Worker Types

The `workerType` field accepts exactly these values:

```text
DELIVERY_RIDER
CONSTRUCTION_WORKER
STREET_VENDOR
WASTE_PICKER
SANITATION_WORKER
```

The frontend may display readable labels such as "Delivery Rider" and "Construction Worker".

The submitted values must remain the exact enum values.

---

## 7. Mobility Types

The `mobility` field accepts exactly these values:

```text
MOTORCYCLE
BICYCLE
HANDCART
WALKING
E_RICKSHAW
```

The frontend may display user-friendly labels.

It must submit the exact enum value expected by the backend.

---

## 8. Complete Workability Response Structure

The actual top-level response DTO is:

```java
WorkabilityResponse(
    DataSource dataSource,
    int workabilityScore,
    Condition condition,
    WorkWindow recommendedWorkWindow,
    String recommendedRouteId,
    EnvironmentSummary environment,
    RouteFactors factors,
    List<RouteResponse> routes
)
```

The work window, environment, and factor objects are nested DTOs.

The frontend must preserve these object structures.

### Example response

The following example illustrates the field structure. Numerical values and route details are sample data, not guaranteed live values.

```json
{
  "dataSource": "live",
  "workabilityScore": 82,
  "condition": "GOOD",
  "recommendedWorkWindow": {
    "start": "21:00",
    "end": "00:00"
  },
  "recommendedRouteId": "route-1",
  "environment": {
    "temperatureCelsius": 28.7,
    "aqi": 77,
    "rainRiskPercent": 29
  },
  "factors": {
    "heatRisk": 25,
    "aqiRisk": 25,
    "rainRisk": 29
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
          [77.2160, 28.6135],
          [77.2295, 28.6129]
        ]
      }
    }
  ]
}
```

The frontend must use the actual response returned by the backend.

It must not assume the sample score, condition, route IDs, work window, or environmental values will always be returned.

---

## 9. Top-Level Response Fields

| Field | Type | Description |
|---|---|---|
| `dataSource` | enum | Source mode of the response |
| `workabilityScore` | integer | Overall score from 0 to 100 |
| `condition` | enum | Overall workability classification |
| `recommendedWorkWindow` | object | Recommended start and end times |
| `recommendedRouteId` | string | ID of the recommended route |
| `environment` | object | Environmental conditions |
| `factors` | object | Backend-provided environmental risk factors |
| `routes` | array | Available route options |

### Recommended work window

The actual response contains an object:

```json
{
  "recommendedWorkWindow": {
    "start": "21:00",
    "end": "00:00"
  }
}
```

The fields are:

| Field | Type | Description |
|---|---|---|
| `start` | string | Start time |
| `end` | string | End time |

The frontend must render:

```js
response.recommendedWorkWindow.start
response.recommendedWorkWindow.end
```

It must not render the entire object as a React child or assume the field is a single string.

A display such as `21:00–00:00` may be constructed from the two returned values.

---

## 10. Environment Object

The actual environment DTO contains:

```java
EnvironmentSummary(
    double temperatureCelsius,
    int aqi,
    int rainRiskPercent
)
```

Example:

```json
{
  "temperatureCelsius": 28.7,
  "aqi": 77,
  "rainRiskPercent": 29
}
```

| Field | Type | Meaning |
|---|---|---|
| `temperatureCelsius` | number | Temperature in Celsius |
| `aqi` | integer | Air Quality Index value |
| `rainRiskPercent` | integer | Backend-provided rain-risk percentage |

**Important:** The field is named `rainRiskPercent`, not `rainProbabilityPercent`.

The frontend must use the exact response field name.

Do not independently calculate rain risk from another value.

---

## 11. Factors Object

The actual factor DTO contains:

```java
RouteFactors(
    int heatRisk,
    int aqiRisk,
    int rainRisk
)
```

Example:

```json
{
  "heatRisk": 25,
  "aqiRisk": 25,
  "rainRisk": 29
}
```

| Field | Type | Description |
|---|---|---|
| `heatRisk` | integer | Backend-provided heat-risk factor |
| `aqiRisk` | integer | Backend-provided AQI-risk factor |
| `rainRisk` | integer | Backend-provided rain-risk factor |

These are not named `heat`, `aqi`, and `rain`.

The frontend must not rename them in the API response model or recalculate them.

If the UI uses friendlier labels, map those labels to the correct fields.

Do not automatically display these values as percentages unless the backend defines them as percentages.

---

## 12. Condition Enum

The `condition` field accepts exactly:

```text
GOOD
MODERATE
DIFFICULT
SEVERE
```

The frontend must display the returned condition.

It must not independently determine the condition from temperature, AQI, rain, or score.

The visual styling may vary by condition, but the underlying value must remain unchanged.

---

## 13. Workability Score

The `workabilityScore` field is an integer from 0 to 100.

Example:

```json
{
  "workabilityScore": 82
}
```

The backend owns the score calculation.

The frontend may use a gauge, number, or other visualization, but it must display the actual value returned by the backend.

It must not adjust the score based on worker type, mobility, route distance, elevation, or environmental values.

---

## 14. Data Source

The `dataSource` field uses the following enum values:

```text
live
cached
demo
```

| Value | Meaning |
|---|---|
| `live` | Backend identifies the response as live data |
| `cached` | Backend identifies the response as cached data |
| `demo` | Backend identifies the response as demo data |

The frontend must display the returned value accurately.

For example:

- `live` → Live Data
- `cached` → Cached Data
- `demo` → Demo Data

These are presentation labels only.

The frontend must not label a response as live when the backend reports `demo` or `cached`.

A frontend mock response must use `demo` and must not be presented as a real live analysis.

The backend's actual data-source behavior is authoritative.

---

## 15. Route Response Structure

Each route is represented by the following DTO:

```java
RouteResponse(
    String routeId,
    double distanceMeters,
    double durationSeconds,
    int score,
    boolean recommended,
    GreenCoverageLevel greenCoverageLevel,
    double totalAscentMeters,
    boolean waterPointNearby,
    List<String> reasons,
    Object geometry
)
```

### Route fields

| Field | Type | Description |
|---|---|---|
| `routeId` | string | Route identifier |
| `distanceMeters` | number | Route distance in metres |
| `durationSeconds` | number | Estimated duration in seconds |
| `score` | integer | Backend-provided route score |
| `recommended` | boolean | Whether the backend marks the route recommended |
| `greenCoverageLevel` | enum | Green coverage classification |
| `totalAscentMeters` | number | Total route ascent in metres |
| `waterPointNearby` | boolean | Whether a water point is nearby |
| `reasons` | array of strings | Backend-generated route reasons |
| `geometry` | object | Route geometry supplied by the backend |

The frontend must not assume a fixed number of routes.

It must handle the routes returned by the API.

If no routes are returned, the UI must handle that state rather than attempting to render a nonexistent route.

---

## 16. Route IDs and Recommendation

The backend provides:

```json
{
  "recommendedRouteId": "route-1"
}
```

Each route also contains a `routeId` and a `recommended` boolean.

The frontend must use the backend's recommendation.

It must not assume that `route-1`, `route-2`, or any other specific ID will always be recommended.

The recommended route should be identified from the returned response.

Route selection in the UI is a presentation interaction. Selecting a route to inspect must not change the backend's workability score or silently replace its recommendation.

If the user selects an alternate route, update the active map route and selected-route presentation.

Preserve the original recommended status separately.

---

## 17. Route Score

Each route has a backend-provided integer score.

Example:

```json
{
  "routeId": "route-1",
  "score": 82
}
```

The frontend displays this value.

It must not independently calculate or rank routes using distance, duration, green coverage, ascent, or other fields.

---

## 18. Green Coverage

The `greenCoverageLevel` field accepts:

```text
LOW
MEDIUM
HIGH
```

Example:

```json
{
  "greenCoverageLevel": "MEDIUM"
}
```

The frontend displays the returned classification.

It must not calculate shade or green coverage from the route geometry.

---

## 19. Ascent and Elevation

The route response contains:

```json
{
  "totalAscentMeters": 14.0
}
```

The value represents total ascent in metres.

The current response contract does not define a complete elevation-profile array.

Therefore, the frontend may:
- Display total ascent
- Show the value in route details
- Use a neutral visual treatment to make the metric easy to scan

The frontend must not:
- Invent an elevation profile
- Derive a fabricated elevation chart from total ascent
- Invent slope percentages
- Claim a route has a steep incline without supporting data

A real elevation profile would require an actual elevation series or another supported source of elevation data.

---

## 20. Water-Point Availability

The route response contains:

```json
{
  "waterPointNearby": true
}
```

This is a boolean.

The frontend may display:
- Water point nearby
- No water point nearby

The current route response does not provide specific water-point coordinates.

Therefore, the frontend must not invent water-point pins or exact point locations on the map.

Specific water-point markers may be added only if a real backend response supplies the required coordinates.

---

## 21. Route Reasons

The route response may include:

```json
{
  "reasons": [
    "Fastest available route"
  ]
}
```

The frontend should render the reasons supplied by the backend.

It must not invent claims such as:
- Safest route
- Best route for heat
- Lowest pollution
- Most shaded route

unless those claims are explicitly supported by backend data.

An empty reasons array is valid:

```json
{
  "reasons": []
}
```

The frontend should handle this without displaying empty bullets or fabricated explanations.

---

## 22. GeoJSON Geometry

The route response contains a geometry object.

Example:

```json
{
  "type": "LineString",
  "coordinates": [
    [77.2090, 28.6139],
    [77.2160, 28.6135],
    [77.2295, 28.6129]
  ]
}
```

The geometry type is `LineString`.

GeoJSON coordinate order is:

```text
[longitude, latitude]
```

It is not:

```text
[latitude, longitude]
```

The frontend must preserve the coordinate order when passing geometry to the map renderer.

The geometry field is currently represented as `Object` in the Java DTO. The frontend should validate the received geometry sufficiently for safe map rendering and handle missing or invalid geometry gracefully.

---

## 23. Units

| Measurement | Unit |
|---|---|
| Temperature | Celsius |
| Distance | Metres |
| Duration | Seconds |
| Total ascent | Metres |
| Workability score | Integer, 0–100 |
| Route score | Integer, 0–100 |
| AQI | Integer |
| Coordinates | Decimal degrees |
| Rain risk | Backend-provided percentage |

The frontend may convert values for display where appropriate, but must not silently reinterpret the underlying response.

For example, seconds may be formatted as minutes and seconds for readability.

---

## 24. Error Response Structure

The global exception handler uses this response structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message"
  }
}
```

The frontend should read the error from:

```js
error.code
error.message
```

after extracting the response body from the HTTP client.

The frontend must account for network errors where no structured response body is available.

Do not assume every failure contains a valid JSON error response.

---

## 25. Error Codes and Statuses

### VALIDATION_ERROR

HTTP status:

```text
400 Bad Request
```

Used for request validation failures, unreadable request bodies, invalid enum values, and empty geocoding queries handled by the current exception handler.

Example:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "mobility: must not be null"
  }
}
```

The exact message may vary.

### NO_ROUTE_FOUND

HTTP status:

```text
404 Not Found
```

Used when the current backend identifies the specific condition that OSRM returned no routes.

Example:

```json
{
  "error": {
    "code": "NO_ROUTE_FOUND",
    "message": "OSRM returned no routes"
  }
}
```

The frontend should explain that no route was found and allow the user to try different locations.

### UPSTREAM_UNAVAILABLE

HTTP status:

```text
503 Service Unavailable
```

Used when an upstream operation fails through the current exception-handling paths.

Example:

```json
{
  "error": {
    "code": "UPSTREAM_UNAVAILABLE",
    "message": "An upstream service is temporarily unavailable."
  }
}
```

The exact message may vary.

The frontend should show an appropriate error and allow retrying when suitable.

### INVALID_LOCATION

`INVALID_LOCATION` is not currently established as an emitted error code by the inspected global exception handler.

Do not assume the backend returns this code.

The frontend may handle it defensively if needed, but it must not rely on it as a confirmed backend behavior until the backend explicitly implements and verifies it.

---

## 26. Loading, Empty, and Error States

The frontend must distinguish these states:

### Loading

The request is in progress.

Show a clear loading indicator and prevent accidental duplicate submissions.

### Successful response

Render the actual response returned by the backend.

### Empty geocoding results

The geocoding request succeeded but returned no suggestions.

Show a helpful no-results message.

### No route found

The backend returned `NO_ROUTE_FOUND`.

Show a contextual message and allow the user to revise the locations.

### Upstream unavailable

The backend returned `UPSTREAM_UNAVAILABLE`.

Show an appropriate error and provide a retry option where possible.

Do not claim that cached data is being used unless a real cached response is available.

### Network error

The backend could not be reached or no structured error response was received.

Show a network/API connection error.

Do not silently replace a failed live request with mock data.

### Dismissible alerts

Error banners should be conditional.

They must not appear permanently in the normal successful dashboard.

---

## 27. Frontend Mock Data

The frontend may use mock data during development and demonstrations.

The mock response must follow the same field names and nested structures as the backend DTO.

In particular:

```json
{
  "recommendedWorkWindow": {
    "start": "17:00",
    "end": "20:00"
  },
  "environment": {
    "temperatureCelsius": 38.5,
    "aqi": 142,
    "rainRiskPercent": 12
  },
  "factors": {
    "heatRisk": 68,
    "aqiRisk": 55,
    "rainRisk": 12
  }
}
```

Do not use the following incorrect alternatives:

```text
recommendedWorkWindow: "17:00-20:00"
environment.rainProbabilityPercent
factors.heat
factors.aqi
factors.rain
```

The mock should include route objects with the same structure as `RouteResponse`.

Mock geometry must use GeoJSON coordinate order.

Mock mode must be clearly indicated as demo data.

Do not mix mock and live fields in a single response or present invented mock values as live information.

---

## 28. Frontend Language Support

The frontend may provide English and Hindi labels.

Language selection changes presentation only.

It must not change:
- Backend enum values
- Request field names
- Response field names
- Endpoint paths
- API semantics

For example, a Hindi label for a delivery rider must still submit:

```json
{
  "workerType": "DELIVERY_RIDER"
}
```

The same principle applies to mobility values.

---

## 29. Non-Negotiable Rules

The following must remain aligned between backend, documentation, mock data, and frontend:

- Endpoint paths and HTTP methods
- Request field names
- Response field names
- Nested object structures
- Enum values
- Units
- Error response structure
- Confirmed error codes
- Data-source values
- Route IDs supplied by the backend
- GeoJSON coordinate order
- Backend-owned scores and recommendations

The frontend must not assume that example values are permanent.

Any intentional backend contract change must be reflected in the documentation, mock data, API integration, and tests.

---

## 30. Integration Verification

Before considering frontend integration complete:

1. Run the backend.
2. Test geocoding.
3. Submit a valid workability request.
4. Inspect the actual JSON response.
5. Confirm the work window is an object containing `start` and `end`.
6. Confirm the environment field is `rainRiskPercent`.
7. Confirm the factors are `heatRisk`, `aqiRisk`, and `rainRisk`.
8. Confirm the routes use the documented fields.
9. Confirm geometry coordinates use `[longitude, latitude]`.
10. Test a validation error.
11. Test route selection and map updates in the frontend.
12. Confirm the UI renders the real response without changing backend values.

**Final rule: The running backend and its DTOs are authoritative. Documentation, mocks, and UI must conform to them.**