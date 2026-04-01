import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import RegisterPage from './pages/RegisterPage';
import TopNPage from './pages/TopNPage';
import PublicationsPage from './pages/PublicationsPage';
import DangerZonePage from './pages/DangerZonePage';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <nav className="navbar">
        <span className="brand">Dashboard Battle</span>
        <div className="nav-links">
          <NavLink to="/" end>Регистрация</NavLink>
          <NavLink to="/topn">ТОП-N</NavLink>
          <NavLink to="/publications">Публикации</NavLink>
          <NavLink to="/danger-zone">Danger Zone</NavLink>
        </div>
      </nav>
      <main className="content">
        <Routes>
          <Route path="/" element={<RegisterPage />} />
          <Route path="/topn" element={<TopNPage />} />
          <Route path="/publications" element={<PublicationsPage />} />
          <Route path="/danger-zone" element={<DangerZonePage />} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
