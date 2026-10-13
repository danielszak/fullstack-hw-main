import { afterEach, describe, expect, it, vi } from "vitest";
import { monitoringService } from "./monitoringService";

const jsonResponse = (body: unknown, status = 200): Response =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

describe("monitoringService", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("returns alerts in newest-first order", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        jsonResponse([
          { id: 1, sensorId: "s1", message: "old", timestamp: "2026-10-09T10:00:00" },
          { id: 2, sensorId: "s1", message: "new", timestamp: "2026-10-09T11:00:00" },
        ]),
      ),
    );

    const result = await monitoringService.getAlerts();

    expect(result.map((alert) => alert.message)).toEqual(["new", "old"]);
  });

  it("posts a reading to the backend and returns the saved reading", async () => {
    const input = { sensorId: "s1", value: 22.4, timestamp: "2026-10-09T12:30" };
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ id: 7, ...input }));
    vi.stubGlobal("fetch", fetchMock);

    await expect(monitoringService.createReading(input)).resolves.toMatchObject(input);

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/sensor-readings",
      expect.objectContaining({ method: "POST", body: JSON.stringify(input) }),
    );
  });

  it("surfaces the backend error message for failed requests", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValue(
          jsonResponse({ message: "The specified sensor was not found." }, 404),
        ),
    );

    await expect(
      monitoringService.createReading({ sensorId: "x", value: 1, timestamp: "2026-10-09T12:30" }),
    ).rejects.toThrow("The specified sensor was not found.");
  });

  it("reports an unreachable backend", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));

    await expect(monitoringService.getSensors()).rejects.toThrow(
      "Unable to reach the monitoring service.",
    );
  });
});
