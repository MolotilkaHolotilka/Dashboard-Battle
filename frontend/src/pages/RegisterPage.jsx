import { useState } from 'react';
import client from '../api/client';
import './RegisterPage.css';

const INITIAL = { email: '', password: '', companyName: '' };

function RegisterPage() {
  const [form, setForm] = useState(INITIAL);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setResult(null);
    setLoading(true);
    try {
      const { data } = await client.post('/auth/register', form);
      setResult(data);
      setForm(INITIAL);
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
          {loading ? 'Загрузка...' : 'Зарегистрироваться'}
        </button>
      </form>

      {error && (
        <div className="alert alert-error">{error}</div>
      )}

      {result && (
        <div className="alert alert-success">
          <strong>Готово!</strong>
          <table className="result-table">
            <tbody>
              <tr><td>User ID</td><td>{result.userId}</td></tr>
              <tr><td>Company ID</td><td>{result.companyId}</td></tr>
              <tr><td>Email</td><td>{result.email}</td></tr>
              <tr><td>Компания</td><td>{result.companyName}</td></tr>
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default RegisterPage;
