import { Link } from "react-router-dom";
import type { Sensor } from "../types";

interface SensorListProps {
  sensors: Sensor[];
}

export function SensorList({ sensors }: SensorListProps) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th scope="col">Sensor</th>
            <th scope="col">Type</th>
            <th scope="col">Action</th>
          </tr>
        </thead>
        <tbody>
          {sensors.map((sensor) => (
            <tr key={sensor.id}>
              <td>
                <span className="sensor-name">{sensor.name}</span>
                <span className="sensor-id">{sensor.id}</span>
              </td>
              <td>
                <span className="type-pill">{sensor.type.replaceAll("_", " ")}</span>
              </td>
              <td>
                <Link className="text-link" to={`/sensors/${sensor.id}`}>
                  Add reading <span aria-hidden="true">→</span>
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
