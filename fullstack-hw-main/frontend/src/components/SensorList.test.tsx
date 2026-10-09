import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { SensorList } from "./SensorList";

describe("SensorList", () => {
  it("shows sensor names and links to their reading form", () => {
    render(
      <MemoryRouter>
        <SensorList
          sensors={[
            { id: "sensor-001", name: "Greenhouse North", type: "TEMPERATURE" },
          ]}
        />
      </MemoryRouter>,
    );

    expect(screen.getByText("Greenhouse North")).toBeInTheDocument();
    expect(screen.getByText("TEMPERATURE")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /add reading/i })).toHaveAttribute(
      "href",
      "/sensors/sensor-001",
    );
  });
});
