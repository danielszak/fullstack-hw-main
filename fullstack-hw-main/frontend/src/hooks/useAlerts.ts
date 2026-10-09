import { useCallback, useEffect, useState } from "react";
import { monitoringService } from "../services/monitoringService";
import type { Alert } from "../types";

const POLL_INTERVAL_MS = 15_000;

interface AlertsState {
  alerts: Alert[];
  isLoading: boolean;
  error: string | null;
  lastUpdated: Date | null;
}

export function useAlerts(): AlertsState {
  const [state, setState] = useState<AlertsState>({
    alerts: [],
    isLoading: true,
    error: null,
    lastUpdated: null,
  });

  const refresh = useCallback(async () => {
    try {
      const alerts = await monitoringService.getAlerts();
      setState({
        alerts,
        isLoading: false,
        error: null,
        lastUpdated: new Date(),
      });
    } catch (error: unknown) {
      setState((current) => ({
        ...current,
        isLoading: false,
        error:
          error instanceof Error ? error.message : "Unable to load alerts.",
      }));
    }
  }, []);

  useEffect(() => {
    void refresh();
    const intervalId = window.setInterval(() => void refresh(), POLL_INTERVAL_MS);
    return () => window.clearInterval(intervalId);
  }, [refresh]);

  return state;
}
