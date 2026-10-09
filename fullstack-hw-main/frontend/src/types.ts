export const SENSOR_TYPES = [
  "TEMPERATURE",
  "HUMIDITY",
  "ATMOSPHERIC_PRESSURE",
] as const;

export type SensorType = (typeof SENSOR_TYPES)[number];

export interface Sensor {
  id: string;
  name: string;
  type: SensorType;
}

export interface SensorReading {
  id: string;
  sensorId: string;
  value: number;
  timestamp: string;
}

export interface NewSensorReading {
  sensorId: string;
  value: number;
  timestamp: string;
}

export interface Alert {
  id: string;
  sensorId: string;
  message: string;
  timestamp: string;
}
