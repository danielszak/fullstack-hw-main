import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { monitoringService } from "../services/monitoringService";
import type { Sensor } from "../types";

interface SensorReadingFormProps {
  sensor: Sensor;
}

const localDateTimeValue = (): string => {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 16);
};

export function SensorReadingForm({ sensor }: SensorReadingFormProps) {
  const navigate = useNavigate();
  const [value, setValue] = useState("");
  const [timestamp, setTimestamp] = useState(localDateTimeValue);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitted(false);

    const numericValue = Number(value);
    if (value.trim() === "" || !Number.isFinite(numericValue)) {
      setError("Enter a valid numeric reading.");
      return;
    }
    if (!timestamp || Number.isNaN(new Date(timestamp).getTime())) {
      setError("Choose a valid date and time.");
      return;
    }

    setIsSubmitting(true);
    try {
      await monitoringService.createReading({
        sensorId: sensor.id,
        value: numericValue,
        timestamp,
      });
      setSubmitted(true);
      setValue("");
      setTimestamp(localDateTimeValue());
    } catch (requestError: unknown) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "The reading could not be submitted. Please try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="reading-form" onSubmit={handleSubmit} noValidate>
      <label htmlFor="reading-value">Reading value</label>
      <div className="input-with-unit">
        <input
          id="reading-value"
          name="value"
          type="number"
          step="any"
          inputMode="decimal"
          value={value}
          onChange={(event) => setValue(event.target.value)}
          placeholder="e.g. 21.5"
          required
        />
        <span className="input-hint">{sensor.type.replaceAll("_", " ")}</span>
      </div>

      <label htmlFor="reading-timestamp">Date and time</label>
      <input
        id="reading-timestamp"
        name="timestamp"
        type="datetime-local"
        value={timestamp}
        onChange={(event) => setTimestamp(event.target.value)}
        required
      />

      {error && <p className="form-message form-message--error" role="alert">{error}</p>}
      {submitted && (
        <p className="form-message form-message--success" role="status">
          Reading saved successfully.
        </p>
      )}

      <div className="form-actions">
        <button className="button button--primary" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving…" : "Save reading"}
        </button>
        <button className="button button--quiet" type="button" onClick={() => navigate("/sensors")}>
          Cancel
        </button>
      </div>
    </form>
  );
}
