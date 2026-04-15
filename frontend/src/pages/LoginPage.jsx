import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import client from '../api/client';
import { saveToken, saveCompanySnapshot } from '../session';
import './LoginPage.css';

function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { data } = await client.post('/auth/login', form);
      saveToken(data.token);
      if (data.userId) localStorage.setItem('lastUserId', String(data.userId));
      if (data.companyId) {
        localStorage.setItem('lastCompanyId', String(data.companyId));
        localStorage.setItem('lastCompanyName', data.companyName ?? '');
        saveCompanySnapshot({ id: data.companyId, name: data.companyName });
      }
      window.dispatchEvent(new Event('auth:login'));
      navigate('/integrations');
    } catch (err) {
      setError(err.response?.data?.message ?? 'Неверный email или пароль');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <h2>Вход</h2>
      <form className="login-form" onSubmit={handleSubmit}>
        <label>
          Email
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="user@example.com"
            required
          />
        </label>
        <label>
          Пароль
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Ваш пароль"
            required
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Вхожу...' : 'Войти'}
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      <p className="login-hint">
        Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
      </p>
    </div>
  );
}

export default LoginPage;
