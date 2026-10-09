import type { Alert, NewSensorReading, Sensor, SensorReading } from "../types";
import { alerts, readings, sensors } from "./mockData";

let nextReadingId = 1;

const delay = (milliseconds: number): Promise<void> =>
  new Promise((resolve) => window.setTimeout(resolve, milliseconds));

export const monitoringService = {
  async getSensors(): Promise<Sensor[]> {
    await delay(120);
    return sensors.map((sensor) => ({ ...sensor }));
  },

  async getAlerts(): Promise<Alert[]> {
    await delay(120);
    return alerts
      .map((alert) => ({ ...alert }))
      .sort(
        (left, right) =>
          new Date(right.timestamp).getTime() -
          new Date(left.timestamp).getTime(),
      );
  },

  async createReading(input: NewSensorReading): Promise<SensorReading> {
    await delay(180);
    if (!sensors.some((sensor) => sensor.id === input.sensorId)) {
      throw new Error("The selected sensor could not be found.");
    }

    const reading: SensorReading = {
      id: `reading-${nextReadingId++}`,
      ...input,
    };
    readings.unshift(reading);
    return { ...reading };
  },
};
