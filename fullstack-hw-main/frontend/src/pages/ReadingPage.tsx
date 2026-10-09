import { Link, Navigate, useParams } from "react-router-dom";
import { PageState } from "../components/PageState";
import { SensorReadingForm } from "../components/SensorReadingForm";
import { useSensors } from "../hooks/useSensors";

export function ReadingPage() {
  const { id } = useParams<{ id: string }>();
  const { sensors, isLoading, error } = useSensors();

  if (isLoading) return <PageState kind="loading" message="Loading sensor…" />;
  if (error) return <PageState kind="error" message={error} />;
  const sensor = sensors.find((item) => item.id === id);
  if (!sensor) return <Navigate to="/sensors" replace />;

  return (
    <section className="narrow-section">
      <Link className="back-link" to="/sensors">← Back to sensors</Link>
      <div className="page-heading page-heading--compact">
        <div>
          <p className="eyebrow">New measurement</p>
          <h1>Add a reading</h1>
          <p className="page-description">
            Record a value for <strong>{sensor.name}</strong>.
          </p>
        </div>
      </div>
      <div className="panel form-panel">
        <SensorReadingForm sensor={sensor} />
      </div>
    </section>
  );
}
