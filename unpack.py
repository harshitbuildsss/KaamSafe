from pathlib import Path

ROOT = Path(__file__).resolve().parent
BACKEND = ROOT / "backend"
FRONTEND = ROOT / "frontend"


def write_file(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists() and path.read_text(encoding="utf-8").strip():
        print(f"[SKIP] Existing non-empty file: {path}")
        return
    path.write_text(content, encoding="utf-8")
    print(f"[CREATED] {path.relative_to(ROOT)}")


def write_empty(path: Path):
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists():
        print(f"[SKIP] Existing file: {path.relative_to(ROOT)}")
        return
    path.write_text("", encoding="utf-8")
    print(f"[CREATED] {path.relative_to(ROOT)}")


# ============================================================
# BACKEND — Spring Boot skeleton
# ============================================================

write_file(BACKEND / "pom.xml", '''<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.6</version>
        <relativePath/>
    </parent>

    <groupId>com.kaamsafe</groupId>
    <artifactId>kaamsafe-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>KaamSafe Backend</name>
    <description>Environmental safety and route recommendation backend</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
''')

write_file(BACKEND / "src/main/resources/application.properties", '''spring.application.name=kaamsafe-backend
server.port=8080

# External services — fill these only when integrations are implemented.
osrm.base-url=https://router.project-osrm.org
open-meteo.base-url=https://api.open-meteo.com
nominatim.base-url=https://nominatim.openstreetmap.org

# live | cached | demo
kaamsafe.data-source-mode=live
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/KaamSafeApplication.java", '''package com.kaamsafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KaamSafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaamSafeApplication.class, args);
    }
}
''')

# DTOs
write_file(BACKEND / "src/main/java/com/kaamsafe/dto/LocationDto.java", '''package com.kaamsafe.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record LocationDto(
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude
) {}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/dto/WorkabilityRequest.java", '''package com.kaamsafe.dto;

import com.kaamsafe.model.Mobility;
import com.kaamsafe.model.WorkerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record WorkabilityRequest(
        @NotNull WorkerType workerType,
        @NotNull Mobility mobility,
        @NotNull @Valid LocationDto origin,
        @NotNull @Valid LocationDto destination
) {}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/dto/WorkabilityResponse.java", '''package com.kaamsafe.dto;

import com.kaamsafe.model.Condition;
import com.kaamsafe.model.DataSource;
import java.util.List;

public record WorkabilityResponse(
        DataSource dataSource,
        int workabilityScore,
        Condition condition,
        WorkWindow recommendedWorkWindow,
        String recommendedRouteId,
        EnvironmentSummary environment,
        RouteFactors factors,
        List<RouteResponse> routes
) {
    public record WorkWindow(String start, String end) {}

    public record EnvironmentSummary(
            double temperatureCelsius,
            int aqi,
            int rainRiskPercent
    ) {}

    public record RouteFactors(
            int heatRisk,
            int aqiRisk,
            int rainRisk
    ) {}
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/dto/RouteResponse.java", '''package com.kaamsafe.dto;

import com.kaamsafe.model.GreenCoverageLevel;
import java.util.List;

public record RouteResponse(
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
) {}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/dto/GeocodeResponse.java", '''package com.kaamsafe.dto;

import java.util.List;

public record GeocodeResponse(List<Result> results) {
    public record Result(String label, double latitude, double longitude) {}
}
''')

# Enums / models
write_file(BACKEND / "src/main/java/com/kaamsafe/model/WorkerType.java", '''package com.kaamsafe.model;

public enum WorkerType {
    DELIVERY_RIDER,
    CONSTRUCTION_WORKER,
    STREET_VENDOR,
    WASTE_PICKER,
    SANITATION_WORKER
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/model/Mobility.java", '''package com.kaamsafe.model;

public enum Mobility {
    MOTORCYCLE,
    BICYCLE,
    HANDCART,
    WALKING,
    E_RICKSHAW
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/model/Condition.java", '''package com.kaamsafe.model;

public enum Condition {
    GOOD,
    MODERATE,
    DIFFICULT,
    SEVERE
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/model/DataSource.java", '''package com.kaamsafe.model;

public enum DataSource {
    live,
    cached,
    demo
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/model/GreenCoverageLevel.java", '''package com.kaamsafe.model;

public enum GreenCoverageLevel {
    LOW,
    MEDIUM,
    HIGH
}
''')

# Controller skeletons
write_file(BACKEND / "src/main/java/com/kaamsafe/controller/WorkabilityController.java", '''package com.kaamsafe.controller;

import com.kaamsafe.dto.WorkabilityRequest;
import com.kaamsafe.dto.WorkabilityResponse;
import com.kaamsafe.service.WorkabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workability")
public class WorkabilityController {

    private final WorkabilityService workabilityService;

    public WorkabilityController(WorkabilityService workabilityService) {
        this.workabilityService = workabilityService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<WorkabilityResponse> analyze(
            @Valid @RequestBody WorkabilityRequest request) {
        return ResponseEntity.ok(workabilityService.analyze(request));
    }
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/controller/GeocodeController.java", '''package com.kaamsafe.controller;

import com.kaamsafe.dto.GeocodeResponse;
import com.kaamsafe.service.GeocodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

    private final GeocodeService geocodeService;

    public GeocodeController(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }

    @GetMapping
    public GeocodeResponse geocode(@RequestParam String query) {
        return geocodeService.geocode(query);
    }
}
''')

# Services / integrations / scoring placeholders
write_file(BACKEND / "src/main/java/com/kaamsafe/service/WorkabilityService.java", '''package com.kaamsafe.service;

import com.kaamsafe.dto.WorkabilityRequest;
import com.kaamsafe.dto.WorkabilityResponse;
import org.springframework.stereotype.Service;

@Service
public class WorkabilityService {

    public WorkabilityResponse analyze(WorkabilityRequest request) {
        // TODO Day 1/2: OSRM -> weather/AQI -> OSM -> elevation -> scoring.
        // Keep all business meaning in the backend. Frontend only renders this response.
        throw new UnsupportedOperationException("Workability engine not implemented yet");
    }
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/service/GeocodeService.java", '''package com.kaamsafe.service;

import com.kaamsafe.dto.GeocodeResponse;
import org.springframework.stereotype.Service;

@Service
public class GeocodeService {

    public GeocodeResponse geocode(String query) {
        // TODO Day 1: proxy Nominatim and add a short cache.
        throw new UnsupportedOperationException("Geocoding not implemented yet");
    }
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/integration/OsrmClient.java", '''package com.kaamsafe.integration;

import org.springframework.stereotype.Component;

@Component
public class OsrmClient {
    // TODO: request overview=full&geometries=geojson and return 2-3 route candidates.
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/integration/OpenMeteoClient.java", '''package com.kaamsafe.integration;

import org.springframework.stereotype.Component;

@Component
public class OpenMeteoClient {
    // TODO: current weather + AQI + hourly forecast for work-window calculation.
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/integration/OsmClient.java", '''package com.kaamsafe.integration;

import org.springframework.stereotype.Component;

@Component
public class OsmClient {
    // TODO: green areas + water/rest POIs.
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/integration/ElevationClient.java", '''package com.kaamsafe.integration;

import org.springframework.stereotype.Component;

@Component
public class ElevationClient {
    // TODO: sample route points and calculate total ascent per route.
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/scoring/ScoreEngine.java", '''package com.kaamsafe.scoring;

import com.kaamsafe.model.Mobility;
import org.springframework.stereotype.Component;

@Component
public class ScoreEngine {

    // v1 weights are mobility-based and explainable. Do not move scoring into React.
    public int score(Mobility mobility,
                     int heatRisk,
                     int aqiRisk,
                     int rainRisk,
                     int effortRisk,
                     int timeRisk,
                     int shadeBenefit) {
        // TODO: implement the frozen weight table from docs/API_CONTRACT.md.
        return 0;
    }
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/scoring/WorkerWeights.java", '''package com.kaamsafe.scoring;

import com.kaamsafe.model.Mobility;

public record WorkerWeights(
        double heat,
        double aqi,
        double rain,
        double effort,
        double time,
        double shade
) {
    public static WorkerWeights forMobility(Mobility mobility) {
        return switch (mobility) {
            case BICYCLE -> new WorkerWeights(0.25, 0.15, 0.20, 0.25, 0.10, 0.05);
            case MOTORCYCLE -> new WorkerWeights(0.15, 0.25, 0.25, 0.05, 0.25, 0.05);
            case HANDCART -> new WorkerWeights(0.25, 0.15, 0.20, 0.25, 0.10, 0.05);
            case WALKING -> new WorkerWeights(0.30, 0.20, 0.20, 0.20, 0.05, 0.05);
            case E_RICKSHAW -> new WorkerWeights(0.15, 0.25, 0.25, 0.05, 0.25, 0.05);
        };
    }
}
''')

# Exceptions
write_file(BACKEND / "src/main/java/com/kaamsafe/exception/ApiError.java", '''package com.kaamsafe.exception;

public record ApiError(ErrorBody error) {
    public record ErrorBody(String code, String message) {}
}
''')

write_file(BACKEND / "src/main/java/com/kaamsafe/exception/GlobalExceptionHandler.java", '''package com.kaamsafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> handleUnsupported(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(new ApiError.ErrorBody("UPSTREAM_UNAVAILABLE", ex.getMessage())));
    }
}
''')

# Tests placeholder
write_file(BACKEND / "src/test/java/com/kaamsafe/KaamSafeApplicationTests.java", '''package com.kaamsafe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KaamSafeApplicationTests {
    @Test
    void contextLoads() {
    }
}
''')

# ============================================================
# FRONTEND — React/Vite skeleton
# ============================================================

write_file(FRONTEND / "package.json", '''{
  "name": "kaamsafe-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@vitejs/plugin-react": "latest",
    "leaflet": "latest",
    "lucide-react": "latest",
    "react": "latest",
    "react-dom": "latest",
    "react-leaflet": "latest"
  },
  "devDependencies": {
    "vite": "latest"
  }
}
''')

write_file(FRONTEND / "vite.config.js", '''import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173
  }
});
''')

write_file(FRONTEND / ".env.example", '''VITE_API_BASE_URL=http://localhost:8080
''')

write_file(FRONTEND / "index.html", '''<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>KaamSafe</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
''')

write_file(FRONTEND / "src/main.jsx", '''import React from 'react';
import ReactDOM from 'react-dom/client';
import 'leaflet/dist/leaflet.css';
import './styles.css';
import App from './App.jsx';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
''')

write_file(FRONTEND / "src/App.jsx", '''import { useState } from 'react';
import { ShieldCheck, MapPinned, ArrowRight } from 'lucide-react';
import { analyzeWorkability } from './services/api.js';

const workers = [
  ['DELIVERY_RIDER', 'Delivery Rider'],
  ['CONSTRUCTION_WORKER', 'Construction Worker'],
  ['STREET_VENDOR', 'Street Vendor'],
  ['WASTE_PICKER', 'Waste Picker'],
  ['SANITATION_WORKER', 'Sanitation Worker']
];

const mobility = [
  ['MOTORCYCLE', 'Motorcycle'],
  ['BICYCLE', 'Bicycle'],
  ['HANDCART', 'Handcart'],
  ['WALKING', 'Walking'],
  ['E_RICKSHAW', 'E-Rickshaw']
];

export default function App() {
  const [workerType, setWorkerType] = useState('DELIVERY_RIDER');
  const [move, setMove] = useState('MOTORCYCLE');
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    // TODO: Day 1 — geocode origin/destination, then call /api/workability/analyze.
    try {
      const response = await analyzeWorkability({
        workerType,
        mobility: move,
        origin,
        destination
      });
      setResult(response);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="shell">
      <header className="brand">
        <span className="brand-mark"><ShieldCheck size={20} /></span>
        <div>
          <strong>KaamSafe</strong>
          <span>Safer routes. Better work days.</span>
        </div>
      </header>

      <section className="hero">
        <div>
          <p className="eyebrow">Environmental Safety Assistant</p>
          <h1>Know when to work.<br />Know which route to take.</h1>
          <p className="subcopy">
            KaamSafe combines environmental conditions and route characteristics
            to recommend a safer option for outdoor workers.
          </p>
        </div>

        <form className="card form" onSubmit={submit}>
          <h2>Plan your work</h2>

          <label>Worker type
            <select value={workerType} onChange={e => setWorkerType(e.target.value)}>
              {workers.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>

          <label>Mobility
            <select value={move} onChange={e => setMove(e.target.value)}>
              {mobility.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>

          <label>From
            <input value={origin} onChange={e => setOrigin(e.target.value)} placeholder="Connaught Place" />
          </label>

          <label>To
            <input value={destination} onChange={e => setDestination(e.target.value)} placeholder="India Gate" />
          </label>

          <button type="submit">Check conditions <ArrowRight size={17} /></button>
          {error && <p className="error">{error}</p>}
        </form>
      </section>

      {result && (
        <section className="card result-placeholder">
          <MapPinned size={20} />
          <div>
            <strong>Backend response connected.</strong>
            <p>Results UI and map rendering will be built against the frozen API contract.</p>
          </div>
        </section>
      )}
    </main>
  );
}
''')

write_file(FRONTEND / "src/services/api.js", '''const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function geocode(query) {
  const response = await fetch(`${API_BASE_URL}/api/geocode?query=${encodeURIComponent(query)}`);
  if (!response.ok) throw new Error('Could not find that location.');
  return response.json();
}

export async function analyzeWorkability({ workerType, mobility, origin, destination }) {
  // TODO: geocode both text locations first, then send the frozen request DTO.
  throw new Error('Workability API is not wired yet.');
}
''')

write_file(FRONTEND / "src/mock/workability-response.json", '''{
  "dataSource": "demo",
  "workabilityScore": 72,
  "condition": "MODERATE",
  "recommendedWorkWindow": { "start": "17:00", "end": "20:00" },
  "recommendedRouteId": "route-2",
  "environment": {
    "temperatureCelsius": 38.5,
    "aqi": 142,
    "rainRiskPercent": 12
  },
  "factors": {
    "heatRisk": 68,
    "aqiRisk": 55,
    "rainRisk": 12
  },
  "routes": []
}
''')

write_file(FRONTEND / "src/components/MapView.jsx", '''export default function MapView() {
  // TODO: render backend GeoJSON route geometry with React Leaflet.
  return <div className="map-placeholder">Map will render route GeoJSON here.</div>;
}
''')

write_file(FRONTEND / "src/components/RouteCard.jsx", '''export default function RouteCard({ route }) {
  // TODO: render score, distance, duration, green coverage, ascent, water point and reasons.
  return <article className="route-card">Route card placeholder</article>;
}
''')

write_file(FRONTEND / "src/components/ScoreCard.jsx", '''export default function ScoreCard({ score, condition }) {
  return (
    <div className="score-card">
      <span>Workability</span>
      <strong>{score ?? '--'}<small>/100</small></strong>
      <em>{condition ?? 'Waiting for analysis'}</em>
    </div>
  );
}
''')

write_file(FRONTEND / "src/styles.css", ''':root {
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  color: #102a26;
  background: #f5f8f7;
  font-synthesis: none;
}

* { box-sizing: border-box; }
body { margin: 0; }
button, input, select { font: inherit; }
.shell { min-height: 100vh; max-width: 1200px; margin: 0 auto; padding: 28px 34px 60px; }
.brand { display: flex; align-items: center; gap: 11px; }
.brand-mark { width: 40px; height: 40px; border-radius: 12px; display: grid; place-items: center; background: #087f73; color: white; }
.brand strong { display: block; font-size: 22px; }
.brand span:last-child { display: block; color: #6b7d79; font-size: 11px; margin-top: 2px; }
.hero { display: grid; grid-template-columns: 1.25fr .75fr; gap: 36px; align-items: center; padding: 80px 0 50px; }
.eyebrow { color: #087f73; font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: .08em; }
h1 { font-size: clamp(40px, 6vw, 68px); line-height: .98; letter-spacing: -2.5px; margin: 14px 0 20px; }
.subcopy { max-width: 650px; color: #667874; line-height: 1.65; font-size: 16px; }
.card { background: white; border: 1px solid #dce7e4; border-radius: 20px; box-shadow: 0 10px 35px rgba(16,42,38,.06); }
.form { padding: 24px; display: grid; gap: 14px; }
.form h2 { margin: 0 0 3px; font-size: 20px; }
.form label { display: grid; gap: 6px; color: #536763; font-size: 12px; font-weight: 700; }
.form input, .form select { width: 100%; border: 1px solid #d5e0dd; border-radius: 10px; padding: 11px 12px; color: #102a26; background: #fff; outline: none; }
.form input:focus, .form select:focus { border-color: #087f73; box-shadow: 0 0 0 3px rgba(8,127,115,.08); }
.form button { border: 0; border-radius: 11px; padding: 12px 15px; background: #087f73; color: white; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; }
.error { color: #a33; font-size: 12px; margin: 0; }
.result-placeholder { padding: 20px; display: flex; align-items: center; gap: 12px; color: #087f73; }
.result-placeholder p { margin: 3px 0 0; color: #6b7d79; font-size: 12px; }
.map-placeholder { height: 380px; display: grid; place-items: center; background: #edf5f2; border-radius: 16px; color: #6b7d79; }
.route-card { padding: 18px; border: 1px solid #dce7e4; border-radius: 14px; }
.score-card { padding: 22px; }
.score-card span, .score-card em { display: block; color: #6b7d79; font-size: 12px; }
.score-card strong { display: block; font-size: 48px; margin: 4px 0; }
.score-card small { font-size: 16px; color: #6b7d79; }
.score-card em { font-style: normal; font-weight: 700; }
@media (max-width: 800px) { .hero { grid-template-columns: 1fr; padding-top: 45px; } .shell { padding: 22px 18px 40px; } }
''')

# ============================================================
# DOCS / ROOT
# ============================================================

write_file(ROOT / "docs/API_CONTRACT.md", '''# KaamSafe API Contract

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
''')

write_file(ROOT / "README.md", '''# KaamSafe

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
''')

print("\n========================================")
print("KaamSafe skeleton created.")
print("========================================")
