import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../api/client';
import { saveToken, saveCompanySnapshot } from '../session';
import './RegisterPage.css';

const INITIAL = { email: '', password: '', companyName: '' };

function RegisterPage() {
  const [form, setForm] = useState(INITIAL);
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
      await client.post('/auth/register', form);
      const { data: loginData } = await client.post('/auth/login', {
        email: form.email,
        password: form.password,
      });
      saveToken(loginData.token);
      if (loginData.userId) localStorage.setItem('lastUserId', String(loginData.userId));
      if (loginData.companyId) {
        localStorage.setItem('lastCompanyId', String(loginData.companyId));
        localStorage.setItem('lastCompanyName', loginData.companyName ?? '');
        saveCompanySnapshot({ id: loginData.companyId, name: loginData.companyName }, loginData.userId);
      }
      window.dispatchEvent(new Event('auth:login'));
      navigate('/integrations');
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Ошибка соединения с сервером';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="register-page">
      <h2>Регистрация</h2>
      <form className="register-form" onSubmit={handleSubmit}>
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
            placeholder="Минимум 6 символов"
            required
          />
        </label>
        <label>
          Название компании
          <input
            type="text"
            name="companyName"
            value={form.companyName}
            onChange={handleChange}
            placeholder="ООО Ромашка"
            required
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Регистрирую...' : 'Зарегистрироваться'}
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      <p className="register-hint">
        Уже есть аккаунт? <Link to="/login">Войти</Link>
      </p>
    </div>
  );
}

export default RegisterPage;
