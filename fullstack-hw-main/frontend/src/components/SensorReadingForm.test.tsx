import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { SensorReadingForm } from "./SensorReadingForm";
import { monitoringService } from "../services/monitoringService";

const sensor = {
  id: "sensor-001",
  name: "Greenhouse North",
  type: "TEMPERATURE" as const,
};

describe("SensorReadingForm", () => {
  it("does not submit an empty reading", async () => {
    const user = userEvent.setup();
    const createReading = vi.spyOn(monitoringService, "createReading");

    render(
      <MemoryRouter>
        <SensorReadingForm sensor={sensor} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "Save reading" }));

    expect(screen.getByRole("alert")).toHaveTextContent("Enter a valid numeric reading.");
    expect(createReading).not.toHaveBeenCalled();
  });

  it("submits valid reading data and confirms success", async () => {
    const user = userEvent.setup();
    const createReading = vi
      .spyOn(monitoringService, "createReading")
      .mockResolvedValue({
        id: "reading-test",
        sensorId: sensor.id,
        value: 22.4,
        timestamp: "2026-10-09T12:30",
      });

    render(
      <MemoryRouter>
        <SensorReadingForm sensor={sensor} />
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText("Reading value"), "22.4");
    await user.click(screen.getByRole("button", { name: "Save reading" }));

    expect(createReading).toHaveBeenCalledWith(
      expect.objectContaining({ sensorId: sensor.id, value: 22.4 }),
    );
    expect(await screen.findByRole("status")).toHaveTextContent("Reading saved successfully.");
  });
});
