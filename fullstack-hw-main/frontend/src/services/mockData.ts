import type { Alert, Sensor, SensorReading } from "../types";

export const sensors: Sensor[] = [
  { id: "sensor-001", name: "Greenhouse North", type: "TEMPERATURE" },
  { id: "sensor-002", name: "Greenhouse South", type: "HUMIDITY" },
  { id: "sensor-003", name: "Roof Station", type: "ATMOSPHERIC_PRESSURE" },
  { id: "sensor-004", name: "Cold Storage", type: "TEMPERATURE" },
];

const minutesAgo = (minutes: number): string =>
  new Date(Date.now() - minutes * 60_000).toISOString();

export const alerts: Alert[] = [
  {
    id: "alert-001",
    sensorId: "sensor-001",
    message: "Temperature exceeded the configured upper limit.",
    timestamp: minutesAgo(8),
  },
  {
    id: "alert-002",
    sensorId: "sensor-002",
    message: "Humidity is below the recommended range.",
    timestamp: minutesAgo(42),
  },
  {
    id: "alert-003",
    sensorId: "sensor-004",
    message: "Sensor stopped reporting for 15 minutes.",
    timestamp: minutesAgo(96),
  },
];

export const readings: SensorReading[] = [];
