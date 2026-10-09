import { AlertList } from "../components/AlertList";
import { PageState } from "../components/PageState";
import { useAlerts } from "../hooks/useAlerts";
import { useSensors } from "../hooks/useSensors";

export function AlertsPage() {
  const alertState = useAlerts();
  const sensorState = useSensors();
  const isLoading = alertState.isLoading || sensorState.isLoading;
  const error = alertState.error ?? sensorState.error;

  return (
    <section>
      <div className="page-heading">
        <div>
          <p className="eyebrow">Things that need attention</p>
          <h1>Alerts</h1>
          <p className="page-description">
            Recent events from across your sensor network.
          </p>
        </div>
        <span className="live-badge"><span /> Live updates · 15 sec</span>
      </div>
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>Recent alerts</h2>
            <p>
              {alertState.lastUpdated
                ? `Last checked at ${alertState.lastUpdated.toLocaleTimeString([], {
                    hour: "numeric",
                    minute: "2-digit",
                  })}`
                : "Checking for updates…"}
            </p>
          </div>
          <span className="count-badge">{alertState.alerts.length} total</span>
        </div>
        {isLoading ? (
          <PageState kind="loading" message="Loading alerts…" />
        ) : error ? (
          <PageState kind="error" message={error} />
        ) : alertState.alerts.length === 0 ? (
          <PageState kind="empty" message="All quiet. There are no alerts right now." />
        ) : (
          <AlertList alerts={alertState.alerts} sensors={sensorState.sensors} />
        )}
      </div>
    </section>
  );
}
