import { useEffect, useState } from "react";
import { monitoringService } from "../services/monitoringService";
import type { Sensor } from "../types";

interface SensorsState {
  sensors: Sensor[];
  isLoading: boolean;
  error: string | null;
}

export function useSensors(): SensorsState {
  const [state, setState] = useState<SensorsState>({
    sensors: [],
    isLoading: true,
    error: null,
  });

  useEffect(() => {
    let isMounted = true;
    monitoringService
      .getSensors()
      .then((sensors) => {
        if (isMounted) setState({ sensors, isLoading: false, error: null });
      })
      .catch((error: unknown) => {
        if (isMounted) {
          setState({
            sensors: [],
            isLoading: false,
            error:
              error instanceof Error ? error.message : "Unable to load sensors.",
          });
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return state;
}
