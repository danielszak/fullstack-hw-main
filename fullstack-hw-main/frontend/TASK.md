# Technical Task: IoT Monitoring Dashboard (React)

## Context

The `backend/` folder contains a Spring Boot application for an IoT sensor
monitoring platform. It exposes (or will expose, as the backend task
progresses) REST endpoints to manage **Sensors**, **Sensor Readings** and
**Alerts**:

- `Sensor`: `id`, `name`, `type` (`TEMPERATURE`, `HUMIDITY`,
  `ATMOSPHERIC_PRESSURE`)
- `SensorReading`: `id`, `sensorId`, `value`, `timestamp`
- `Alert`: `id`, `sensorId`, `message`, `timestamp` (raised automatically by
  the backend when a reading is out of range)

Known/expected endpoints:

- `GET /sensors` / `POST /sensors`
- `GET /sensor-readings` / `POST /sensor-readings`
- `GET /alerts`

You are free to run the backend locally (`backend/docker-compose.yml` +
`./mvnw spring-boot:run`) and adapt to whatever shape the endpoints actually
return. If an endpoint you need is missing, mock it and document the
assumption in your README.

## Goal

Build a small React dashboard that lists sensors, lets a user submit new
sensor readings, and displays alerts. You don't need to finish everything;
a well-tested subset beats a rushed complete solution.

## Tasks

1. **Project setup & Sensors list**
   Scaffold the app and create a `/sensors` page that fetches and displays
   all sensors from the backend in a table/list (name + type).

2. **Add a sensor reading**
   On a `/sensors/:id` or `/sensor-readings` page, build a form to
   submit a new reading (`value`, `timestamp`) for a selected sensor, with
   basic validation and error handling for failed requests.

3. **Alerts page with live-ish updates**
   Create an `/alerts` page listing alerts (message, sensor, timestamp),
   sorted by most recent first. Poll the endpoint, so new alerts appear without a manual refresh.

4. **State & data layer design**
   Introduce a clean separation between HTTP services, a state layer and
   presentational components.

5. **Testing**
   Add unit tests for at least one service and one component.

6. **Bonus: resilience & UX polish**
   Handle loading/error/empty states consistently across pages, add basic
   responsiveness, and describe (in the README) how you'd scale this to
   handle high-frequency sensor data.

## Deliverables

- A short **README** explaining how to run the app, decisions made, and any
  assumptions about the backend API.
- Source code (React app) with the tasks above implemented as far as you
  got.

Please don't spend more than a few hours on this — we care about quality and
reasoning over completeness.
