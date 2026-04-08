import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import RegisterPage from './pages/RegisterPage';
import TopNPage from './pages/TopNPage';
import PublicationsPage from './pages/PublicationsPage';
import IntegrationsPage from './pages/IntegrationsPage';
import DangerZonePage from './pages/DangerZonePage';
import { dangerZoneEnabled } from './config';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <nav className="navbar">
        <span className="brand">Dashboard Battle</span>
        <div className="nav-links">
          <NavLink to="/" end>Регистрация</NavLink>
          <NavLink to="/integrations">Интеграции</NavLink>
          <NavLink to="/topn">ТОП-N</NavLink>
          <NavLink to="/publications">Публикации</NavLink>
          {dangerZoneEnabled && (
            <NavLink to="/danger-zone" className="nav-danger">Danger Zone</NavLink>
          )}
        </div>
      </nav>
      <main className="content">
        <Routes>
          <Route path="/" element={<RegisterPage />} />
          <Route path="/integrations" element={<IntegrationsPage />} />
          <Route path="/topn" element={<TopNPage />} />
          <Route path="/publications" element={<PublicationsPage />} />
          {dangerZoneEnabled && (
            <Route path="/danger-zone" element={<DangerZonePage />} />
          )}
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
