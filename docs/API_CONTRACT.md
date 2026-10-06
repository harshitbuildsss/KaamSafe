# KaamSafe API Contract

Source of truth for backend/frontend integration.

## Golden rule

Backend owns meaning. Frontend owns presentation. React never calls OSRM, Open-Meteo, OSM or Nominatim directly.

## Endpoints

### `POST /api/workability/analyze`

Request:

```json
{
  "workerType": "DELIVERY_RIDER",
  "mobility": "MOTORCYCLE",
  "origin": { "latitude": 28.6139, "longitude": 77.2090 },
  "destination": { "latitude": 28.6129, "longitude": 77.2295 }
}
```

Response fields are defined by the shared contract document. GeoJSON route coordinates use `[longitude, latitude]`.

### `GET /api/geocode?query=...`

Backend proxies Nominatim. Frontend never calls Nominatim directly.

## Enums

WorkerType: `DELIVERY_RIDER`, `CONSTRUCTION_WORKER`, `STREET_VENDOR`, `WASTE_PICKER`, `SANITATION_WORKER`

Mobility: `MOTORCYCLE`, `BICYCLE`, `HANDCART`, `WALKING`, `E_RICKSHAW`

Condition: `GOOD`, `MODERATE`, `DIFFICULT`, `SEVERE`

dataSource: `live`, `cached`, `demo`

greenCoverageLevel: `LOW`, `MEDIUM`, `HIGH`

## Units

Temperature: °C
Distance: metres
Duration: seconds
Elevation/ascent: metres
Score: integer 0–100
AQI: integer
Coordinates: decimal degrees
