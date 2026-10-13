import type { Alert, NewSensorReading, Sensor, SensorReading } from "../types";

const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? "/api";

async function readErrorMessage(response: Response): Promise<string> {
  try {
    const body: unknown = await response.json();
    if (
      typeof body === "object" &&
      body !== null &&
      "message" in body &&
      typeof body.message === "string" &&
      body.message !== ""
    ) {
      return body.message;
    }
  } catch {
    // Non-JSON error body; fall through to the generic message.
  }
  return `The request failed (HTTP ${response.status}).`;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, init);
  } catch {
    throw new Error("Unable to reach the monitoring service.");
  }
  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }
  return (await response.json()) as T;
}

export const monitoringService = {
  getSensors(): Promise<Sensor[]> {
    return request<Sensor[]>("/sensors");
  },

  async getAlerts(): Promise<Alert[]> {
    const alerts = await request<Alert[]>("/alerts");
    return [...alerts].sort(
      (left, right) =>
        new Date(right.timestamp).getTime() - new Date(left.timestamp).getTime(),
    );
  },

  createReading(input: NewSensorReading): Promise<SensorReading> {
    return request<SensorReading>("/sensor-readings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    });
  },
};
