import { PageState } from "../components/PageState";
import { SensorList } from "../components/SensorList";
import { useSensors } from "../hooks/useSensors";

export function SensorsPage() {
  const { sensors, isLoading, error } = useSensors();

  return (
    <section>
      <div className="page-heading">
        <div>
          <p className="eyebrow">Your connected devices</p>
          <h1>Sensors</h1>
          <p className="page-description">
            Keep an eye on your sensor fleet and record a new measurement.
          </p>
        </div>
        <span className="count-badge">{sensors.length} devices</span>
      </div>
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>All sensors</h2>
            <p>Name and measurement type for each registered sensor.</p>
          </div>
        </div>
        {isLoading ? (
          <PageState kind="loading" message="Loading sensors…" />
        ) : error ? (
          <PageState kind="error" message={error} />
        ) : sensors.length === 0 ? (
          <PageState kind="empty" message="No sensors have been registered yet." />
        ) : (
          <SensorList sensors={sensors} />
        )}
      </div>
    </section>
  );
}
