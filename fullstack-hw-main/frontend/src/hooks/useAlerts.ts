import { useEffect, useState } from "react";
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

  useEffect(() => {
    let isMounted = true;
    let isRefreshing = false;

    const refresh = async () => {
      if (isRefreshing) return;
      isRefreshing = true;

      try {
        const alerts = await monitoringService.getAlerts();
        if (isMounted) {
          setState({
            alerts,
            isLoading: false,
            error: null,
            lastUpdated: new Date(),
          });
        }
      } catch (error: unknown) {
        if (isMounted) {
          setState((current) => ({
            ...current,
            isLoading: false,
            error:
              error instanceof Error ? error.message : "Unable to load alerts.",
          }));
        }
      } finally {
        isRefreshing = false;
      }
    };

    void refresh();
    const intervalId = window.setInterval(() => void refresh(), POLL_INTERVAL_MS);
    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, []);

  return state;
}
