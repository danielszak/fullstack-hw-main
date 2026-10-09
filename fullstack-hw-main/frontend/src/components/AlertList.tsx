import type { Alert, Sensor } from "../types";

interface AlertListProps {
  alerts: Alert[];
  sensors: Sensor[];
}

const formatTimestamp = (timestamp: string): string =>
  new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(timestamp));

export function AlertList({ alerts, sensors }: AlertListProps) {
  const sensorNames = new Map(sensors.map((sensor) => [sensor.id, sensor.name]));

  return (
    <div className="alert-list">
      {alerts.map((alert) => (
        <article className="alert-card" key={alert.id}>
          <span className="alert-indicator" aria-hidden="true" />
          <div className="alert-card__body">
            <p className="alert-message">{alert.message}</p>
            <p className="alert-meta">
              <span>{sensorNames.get(alert.sensorId) ?? "Unknown sensor"}</span>
              <span aria-hidden="true">·</span>
              <time dateTime={alert.timestamp}>{formatTimestamp(alert.timestamp)}</time>
            </p>
          </div>
          <span className="alert-label">Alert</span>
        </article>
      ))}
    </div>
  );
}
