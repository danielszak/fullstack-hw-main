import { act, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { AlertsPage } from "./AlertsPage";
import { monitoringService } from "../services/monitoringService";
import type { Alert } from "../types";

describe("AlertsPage", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it("shows alerts from later polls and stops polling when unmounted", async () => {
    vi.useFakeTimers();
    vi.spyOn(monitoringService, "getSensors").mockResolvedValue([]);
    let resolveSecondPoll!: (alerts: Alert[]) => void;
    const getAlerts = vi
      .spyOn(monitoringService, "getAlerts")
      .mockResolvedValueOnce([
        {
          id: "alert-001",
          sensorId: "sensor-001",
          message: "Initial alert",
          timestamp: "2026-10-09T10:00:00Z",
        },
      ])
      .mockImplementation(
        () =>
          new Promise((resolve) => {
            resolveSecondPoll = resolve;
          }),
      );

    const { unmount } = render(<AlertsPage />);
    await act(async () => {
      await Promise.resolve();
    });
    expect(screen.getByText("Initial alert")).toBeInTheDocument();

    await act(async () => {
      await vi.advanceTimersByTimeAsync(15_000);
    });
    await act(async () => {
      await vi.advanceTimersByTimeAsync(15_000);
    });
    expect(getAlerts).toHaveBeenCalledTimes(2);

    await act(async () => {
      resolveSecondPoll([
        {
          id: "alert-002",
          sensorId: "sensor-001",
          message: "New alert from polling",
          timestamp: "2026-10-09T10:15:00Z",
        },
        {
          id: "alert-001",
          sensorId: "sensor-001",
          message: "Initial alert",
          timestamp: "2026-10-09T10:00:00Z",
        },
      ]);
      await Promise.resolve();
    });
    expect(screen.getByText("New alert from polling")).toBeInTheDocument();
    expect(getAlerts).toHaveBeenCalledTimes(2);

    unmount();
    await act(async () => {
      await vi.advanceTimersByTimeAsync(15_000);
    });
    expect(getAlerts).toHaveBeenCalledTimes(2);
  });
});
