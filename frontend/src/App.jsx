import { BrowserRouter, Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import TopNPage from './pages/TopNPage';
import PublicationsPage from './pages/PublicationsPage';
import IntegrationsPage from './pages/IntegrationsPage';
import DangerZonePage from './pages/DangerZonePage';
import { dangerZoneEnabled } from './config';
import { isAuthenticated, clearAuth } from './session';
import './App.css';

function NavBar({ auth, onLogout }) {
  return (
    <nav className="navbar">
      <span className="brand">Dashboard Battle</span>
      <div className="nav-links">
        {!auth && <NavLink to="/login">Войти</NavLink>}
        {!auth && <NavLink to="/register">Регистрация</NavLink>}
        {auth && <NavLink to="/integrations">Интеграции</NavLink>}
        {auth && <NavLink to="/topn">ТОП-N</NavLink>}
        {auth && <NavLink to="/publications">Публикации</NavLink>}
        {auth && dangerZoneEnabled && (
          <NavLink to="/danger-zone" className="nav-danger">Danger Zone</NavLink>
        )}
      </div>
      {auth && (
        <div className="nav-right">
          <button className="nav-logout" onClick={onLogout}>Выйти</button>
        </div>
      )}
    </nav>
  );
}

function App() {
  const [auth, setAuth] = useState(() => isAuthenticated());

  useEffect(() => {
    function onLogin() { setAuth(true); }
    function onLogout() { setAuth(false); }

    window.addEventListener('auth:login', onLogin);
    window.addEventListener('auth:logout', onLogout);
    window.addEventListener('storage', () => setAuth(isAuthenticated()));
    return () => {
      window.removeEventListener('auth:login', onLogin);
      window.removeEventListener('auth:logout', onLogout);
    };
  }, []);

  function handleLogout() {
    clearAuth();
    setAuth(false);
    window.dispatchEvent(new Event('auth:logout'));
  }

  function protectedRoute(element) {
    return auth ? element : <Navigate to="/login" replace />;
  }

  return (
    <BrowserRouter>
      <NavBar auth={auth} onLogout={handleLogout} />
      <main className="content">
        <Routes>
          <Route path="/login" element={auth ? <Navigate to="/integrations" replace /> : <LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={auth ? <Navigate to="/integrations" replace /> : <Navigate to="/login" replace />} />
          <Route path="/integrations" element={protectedRoute(<IntegrationsPage />)} />
          <Route path="/topn" element={protectedRoute(<TopNPage />)} />
          <Route path="/publications" element={protectedRoute(<PublicationsPage />)} />
          {dangerZoneEnabled && (
            <Route path="/danger-zone" element={protectedRoute(<DangerZonePage />)} />
          )}
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
