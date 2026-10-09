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
stateful hooks, and a monitoring service. Types for sensors, readings, and
alerts live in `src/types.ts`; the service currently uses seeded in-memory mock
data in `src/services/mockData.ts`.

The backend currently exposes `POST /sensors` and `POST /sensor-readings`, but
does not yet expose the `GET /sensors`, `GET /alerts`, or `GET /sensor-readings`
collection endpoints expected by this dashboard. In addition, reading
persistence is still a TODO in the backend service. As requested, **all
dashboard data operations are mocked**: sensors and alerts are seeded locally,
submitted readings stay in memory for the running browser session, and alerts
are polled from the mock service every 15 seconds. No backend process or CORS
configuration is required for the current demo.

The mock response shapes follow the task brief. Sensor IDs are strings,
`type` is one of `TEMPERATURE`, `HUMIDITY`, or `ATMOSPHERIC_PRESSURE`, reading
timestamps are date-time strings, and alerts refer to sensors by `sensorId`.
The reading form sends the equivalent of
`{ sensorId, value, timestamp }`. When the backend endpoints are ready, replace
the implementation in `monitoringService.ts` with typed HTTP requests while
keeping the component and hook contracts unchanged.

## High-frequency data

The 15-second alert poll is suitable only for a small demo. At higher volumes,
use server-sent events or WebSockets for incremental alert delivery; add
pagination or time-window queries for history; aggregate/downsample readings
before sending them to the browser; and virtualize long tables and lists.
Backend-side retention, rate limits, and bounded polling intervals would also
keep data volume and resource use predictable.
