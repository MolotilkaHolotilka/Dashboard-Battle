import { useState } from 'react';
import client from '../api/client';
import './TopNPage.css';

const STATUS_LABEL = {
  PENDING: 'Ожидает подтверждения',
  CONFIRMED: 'Подтверждён',
  PUBLISHING: 'Публикуется',
  PUBLISHED: 'Опубликован',
  FAILED: 'Ошибка',
  RECALLED: 'Отозван',
};

function StatusBadge({ status }) {
  return <span className={`badge badge-${status?.toLowerCase()}`}>{STATUS_LABEL[status] ?? status}</span>;
}

function TopNPage() {
  const [companyId, setCompanyId] = useState(() => localStorage.getItem('lastCompanyId') ?? '');
  const [topN, setTopN] = useState(5);
  const [report, setReport] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [confirming, setConfirming] = useState(false);

  async function handleRequest(e) {
    e.preventDefault();
    setError(null);
    setReport(null);
    setLoading(true);
    try {
      const { data } = await client.post(`/reports/top-n/request/${companyId}`, null, {
        params: { topN },
      });
      setReport(data);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка соединения с сервером');
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirm() {
    setError(null);
    setConfirming(true);
    try {
      const { data } = await client.post(`/reports/top-n/${report.id}/confirm`);
      setReport(data);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка при подтверждении');
    } finally {
      setConfirming(false);
    }
  }

  return (
    <div className="topn-page">
      <h2>ТОП-N сотрудников</h2>

      <form className="topn-form" onSubmit={handleRequest}>
        <label>
          ID компании
          <input
            type="number"
            value={companyId}
            onChange={e => setCompanyId(e.target.value)}
            placeholder="из регистрации"
            required
            min="1"
          />
        </label>
        <label>
          Количество (N)
          <input
            type="number"
            value={topN}
            onChange={e => setTopN(Number(e.target.value))}
            min="1"
            max="50"
            required
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Запрос...' : 'Запросить ТОП-N'}
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      {report && (
        <div className="report-card">
          <div className="report-header">
            <div className="report-meta">
              <span>Отчёт <strong>#{report.id}</strong></span>
              <span>
                Компания:{' '}
                <strong title={`id ${report.companyId}`}>
                  {report.companyName ? `${report.companyName} (${report.companyId})` : report.companyId}
                </strong>
              </span>
              {report.periodStart && (
                <span>Период: {report.periodStart} — {report.periodEnd}</span>
              )}
            </div>
            <StatusBadge status={report.status} />
          </div>

          {report.entries && report.entries.length > 0 ? (
            <table className="entries-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Сотрудник</th>
                  <th>Выручка</th>
                  <th>Маржа</th>
                  <th>Топ-продукт</th>
                </tr>
              </thead>
              <tbody>
                {report.entries.map(entry => (
                  <tr key={entry.id ?? entry.rank}>
                    <td className="rank">{entry.rank}</td>
                    <td>{entry.employeeName}</td>
                    <td className="number">{entry.revenue?.toLocaleString('ru-RU')} ₽</td>
                    <td className="number">{entry.margin?.toLocaleString('ru-RU')} ₽</td>
                    <td>{entry.favoriteProduct ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="empty">Нет данных в отчёте</p>
          )}

          {report.status === 'PENDING' && (
            <button
              className="btn-confirm"
              onClick={handleConfirm}
              disabled={confirming}
            >
              {confirming ? 'Подтверждаю...' : 'Подтвердить отчёт'}
            </button>
          )}
        </div>
      )}
    </div>
  );
}

export default TopNPage;
