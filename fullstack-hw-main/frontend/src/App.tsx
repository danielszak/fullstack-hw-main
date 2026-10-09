import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { AlertsPage } from "./pages/AlertsPage";
import { ReadingPage } from "./pages/ReadingPage";
import { SensorsPage } from "./pages/SensorsPage";

function App() {
  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar__inner">
          <NavLink className="brand" to="/sensors" aria-label="Fieldnote home">
            <span className="brand-mark" aria-hidden="true">F</span>
            <span>fieldnote<span className="brand-period">.</span></span>
          </NavLink>
          <nav className="main-nav" aria-label="Main navigation">
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? " nav-link--active" : ""}`}
              to="/sensors"
            >
              Sensors
            </NavLink>
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? " nav-link--active" : ""}`}
              to="/alerts"
            >
              Alerts
            </NavLink>
          </nav>
          <div className="connection-status">
            <span className="connection-dot" />
            <span>Demo environment</span>
          </div>
        </div>
      </header>
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Navigate to="/sensors" replace />} />
          <Route path="/sensors" element={<SensorsPage />} />
          <Route path="/sensors/:id" element={<ReadingPage />} />
          <Route path="/alerts" element={<AlertsPage />} />
          <Route path="*" element={<Navigate to="/sensors" replace />} />
        </Routes>
      </main>
      <footer className="footer">
        <span>Fieldnote Monitoring</span>
        <span>Sensor operations, made clear.</span>
      </footer>
    </div>
  );
}

export default App;
