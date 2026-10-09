import { describe, expect, it } from "vitest";
import { monitoringService } from "./monitoringService";

describe("monitoringService", () => {
  it("returns alerts in newest-first order", async () => {
    const result = await monitoringService.getAlerts();
    const timestamps = result.map((alert) => Date.parse(alert.timestamp));

    expect(timestamps).toEqual([...timestamps].sort((a, b) => b - a));
  });

  it("creates and returns a reading for a known sensor", async () => {
    const input = {
      sensorId: "sensor-001",
      value: 22.4,
      timestamp: "2026-10-09T12:30",
    };

    await expect(monitoringService.createReading(input)).resolves.toMatchObject(input);
  });

  it("rejects a reading for an unknown sensor", async () => {
    await expect(
      monitoringService.createReading({
        sensorId: "missing",
        value: 1,
        timestamp: "2026-10-09T12:30",
      }),
    ).rejects.toThrow("The selected sensor could not be found.");
  });
});
