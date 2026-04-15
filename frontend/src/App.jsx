import { BrowserRouter, Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import RegisterPage from './pages/RegisterPage';
import TopNPage from './pages/TopNPage';
import PublicationsPage from './pages/PublicationsPage';
import IntegrationsPage from './pages/IntegrationsPage';
import DangerZonePage from './pages/DangerZonePage';
import { dangerZoneEnabled } from './config';
import { isRegisteredLocally } from './session';
import './App.css';

function App() {
  const [hasRegistration, setHasRegistration] = useState(() => isRegisteredLocally());

  useEffect(() => {
    function syncRegistration() {
      setHasRegistration(isRegisteredLocally());
    }
    window.addEventListener('registration:changed', syncRegistration);
    window.addEventListener('storage', syncRegistration);
    return () => {
      window.removeEventListener('registration:changed', syncRegistration);
      window.removeEventListener('storage', syncRegistration);
    };
  }, []);

  function protectedRoute(element) {
    if (hasRegistration) {
      return element;
    }
    return <Navigate to="/" replace />;
  }

  return (
    <BrowserRouter>
      <nav className="navbar">
        <span className="brand">Dashboard Battle</span>
        <div className="nav-links">
          <NavLink to="/" end>Регистрация</NavLink>
          {hasRegistration && <NavLink to="/integrations">Интеграции</NavLink>}
          {hasRegistration && <NavLink to="/topn">ТОП-N</NavLink>}
          {hasRegistration && <NavLink to="/publications">Публикации</NavLink>}
          {hasRegistration && dangerZoneEnabled && (
            <NavLink to="/danger-zone" className="nav-danger">Danger Zone</NavLink>
          )}
        </div>
      </nav>
      <main className="content">
        {!hasRegistration && (
          <div className="access-note">
            После регистрации откроются вкладки «Интеграции», «ТОП-N» и «Публикации».
          </div>
        )}
        <Routes>
          <Route path="/" element={<RegisterPage />} />
          <Route path="/integrations" element={protectedRoute(<IntegrationsPage />)} />
          <Route path="/topn" element={protectedRoute(<TopNPage />)} />
          <Route path="/publications" element={protectedRoute(<PublicationsPage />)} />
          {dangerZoneEnabled && hasRegistration && (
            <Route path="/danger-zone" element={protectedRoute(<DangerZonePage />)} />
          )}
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
