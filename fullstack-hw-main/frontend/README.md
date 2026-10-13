# IoT Monitoring Dashboard

A responsive React and TypeScript dashboard for monitoring sensors, recording
readings, and viewing recent alerts.

## Requirements

- Node.js 24 LTS or newer
- npm

## Run locally

From this directory:

```sh
npm install
npm run dev
```

Vite prints the local development URL after startup. To run checks and produce
a production build:

```sh
npm test
npm run build
npm run preview
```

## Implementation and API assumptions

The UI is split into route-level pages, reusable presentational components,
stateful hooks, and an HTTP service (`src/services/monitoringService.ts`).
Types for sensors, readings, and alerts live in `src/types.ts`.

The dashboard talks to the Spring Boot backend (`../backend`, port 8080) using
`GET /sensors`, `GET /alerts` and `POST /sensor-readings`. In development, Vite
proxies `/api/*` to `http://localhost:8080/*`, so no CORS configuration is
needed; set `VITE_API_BASE_URL` to call a different base URL. Start the
backend first (`docker compose up -d` and `./mvnw spring-boot:run` in
`../backend`); the database starts empty, so create a sensor with
`POST /sensors` (`{"name": "...", "type": "TEMPERATURE"}`) before using the UI.

Assumptions: `type` is one of `TEMPERATURE`, `HUMIDITY`, or
`ATMOSPHERIC_PRESSURE`; timestamps are ISO local date-time strings without a
zone (as the backend uses `LocalDateTime`); alerts refer to sensors by
`sensorId`. Alerts are polled every 15 seconds and sorted newest first.
Backend error messages (`message` field) are shown in the UI.

## High-frequency data

The 15-second alert poll is suitable only for a small demo. At higher volumes,
use server-sent events or WebSockets for incremental alert delivery; add
pagination or time-window queries for history; aggregate/downsample readings
before sending them to the browser; and virtualize long tables and lists.
Backend-side retention, rate limits, and bounded polling intervals would also
keep data volume and resource use predictable.
