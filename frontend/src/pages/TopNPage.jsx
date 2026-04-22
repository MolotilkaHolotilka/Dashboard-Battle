import { useCallback, useEffect, useState } from 'react';
import client from '../api/client';
import useUserCompanies from '../hooks/useUserCompanies';
import './TopNPage.css';

const STATUS_LABEL = {
  PENDING:    'Ожидает вашего подтверждения на публикацию',
  CONFIRMED:  'Подтверждён',
  PUBLISHING: 'Публикуется',
  PUBLISHED:  'Опубликован',
  FAILED:     'Ошибка',
  PUBLISH_FAILED: 'Ошибка публикации',
  RECALLED:   'Отозван',
  ARCHIVED:   'В архиве',
};

const STATUS_HINT = {
  PENDING:   'Рейтинг сформирован системой и ожидает вашего подтверждения на публикацию. Нажмите «Подтвердить отчёт» ниже.',
  CONFIRMED: 'Рейтинг подтверждён и готов к публикации в разделе «Публикации».',
  PUBLISHING:'Идёт публикация рейтинга в выбранный канал.',
  PUBLISHED: 'Рейтинг успешно опубликован.',
  FAILED:    'Ошибка публикации. Попробуйте ещё раз или обратитесь к администратору.',
  PUBLISH_FAILED: 'Ошибка публикации. Отчёт можно скорректировать или убрать в архив.',
  RECALLED:  'Публикация была отозвана.',
  ARCHIVED:  'Отчёт перемещён в архив и скрыт из основного списка.',
};

function StatusBadge({ status }) {
  return (
    <span
      className={`badge badge-${status?.toLowerCase()}`}
      title={STATUS_HINT[status] ?? status}
    >
      {STATUS_LABEL[status] ?? status}
    </span>
  );
}

function TopNPage() {
  const [companyId, setCompanyId] = useState(() => localStorage.getItem('lastCompanyId') ?? '');
  const [topN, setTopN] = useState(5);
  const [reports, setReports] = useState([]);
  const [report, setReport] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [archiving, setArchiving] = useState(false);
  const [reportsLoading, setReportsLoading] = useState(false);
  const { companies } = useUserCompanies();

  const loadReports = useCallback(async (cid) => {
    if (!cid) {
      setReports([]);
      setReport(null);
      return;
    }

    setReportsLoading(true);
    try {
      const { data } = await client.get('/reports/top-n', { params: { companyId: cid } });
      const nextReports = data ?? [];
      setReports(nextReports);
      setReport((prevReport) => {
        if (!prevReport) {
          return nextReports[0] ?? null;
        }
        const refreshed = nextReports.find(item => String(item.id) === String(prevReport.id));
        return refreshed ?? (nextReports[0] ?? null);
      });
    } catch {
      setReports([]);
      setReport(null);
    } finally {
      setReportsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (companyId) {
      localStorage.setItem('lastCompanyId', String(companyId));
    }
    loadReports(companyId);
  }, [companyId, loadReports]);

  function formatDateTime(value) {
    if (!value) return '—';
    const date = new Date(value);
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

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
      setReports(prev => [data, ...prev.filter(item => String(item.id) !== String(data.id))]);
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
      setReports(prev => prev.map(item => (
        String(item.id) === String(data.id) ? data : item
      )));
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка при подтверждении');
    } finally {
      setConfirming(false);
    }
  }

  async function handleArchive() {
    if (!report) {
      return;
    }
    setError(null);
    setArchiving(true);
    try {
      const { data } = await client.post(`/reports/top-n/${report.id}/archive`);
      setReports(prev => {
        const nextReports = prev.filter(item => String(item.id) !== String(data.id));
        setReport(current => (
          String(current?.id) === String(data.id) ? (nextReports[0] ?? null) : current
        ));
        return nextReports;
      });
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка при архивировании');
    } finally {
      setArchiving(false);
    }
  }

  const canArchive = report && report.status !== 'PUBLISHING' && report.status !== 'ARCHIVED';

  return (
    <div className="topn-page">
      <h2>ТОП-N сотрудников</h2>

      <form className="topn-form" onSubmit={handleRequest}>
        <label>
          Компания
          <select
            value={companyId}
            onChange={e => setCompanyId(e.target.value)}
            required
            title="Компания из МойСклад, по сотрудникам которой будет строиться рейтинг"
          >
            <option value="">— выберите компанию —</option>
            {companies.map(company => (
              <option key={company.id} value={company.id}>
                {company.name}
              </option>
            ))}
          </select>
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
            title="Сколько лучших сотрудников включить в рейтинг (1–50). Сортировка по выручке."
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Запрос...' : 'Запросить ТОП-N'}
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="topn-history">
        <h3>Ранее сформированные отчёты</h3>
        {!companyId ? (
          <p className="empty">Выберите компанию, чтобы увидеть сформированные отчёты</p>
        ) : reportsLoading ? (
          <p className="empty">Загрузка отчётов...</p>
        ) : reports.length === 0 ? (
          <p className="empty">По выбранной компании пока нет сформированных отчётов</p>
        ) : (
          <ul className="report-list">
            {reports.map(item => (
              <li key={item.id}>
                <button
                  type="button"
                  className={`report-pick ${String(report?.id) === String(item.id) ? 'active' : ''}`}
                  onClick={() => setReport(item)}
                  title={STATUS_HINT[item.status] ?? item.status}
                >
                  <span>Отчёт #{item.id}</span>
                  <span>{formatDateTime(item.createdAt)}</span>
                  <StatusBadge status={item.status} />
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

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
                  <th title="Порядковое место в рейтинге">#</th>
                  <th title="ФИО сотрудника из МойСклад">Сотрудник</th>
                  <th title="Суммарная выручка сотрудника за период">Выручка</th>
                  <th title="Маржа: выручка минус себестоимость товаров">Маржа</th>
                  <th title="Товар, который этот сотрудник продаёт чаще всего">Топ-продукт</th>
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

          <div className="report-actions">
            {report.status === 'PENDING' && (
              <button
                className="btn-confirm"
                onClick={handleConfirm}
                disabled={confirming || archiving}
              >
                {confirming ? 'Подтверждаю...' : 'Подтвердить отчёт'}
              </button>
            )}
            {canArchive && (
              <button
                className="btn-archive"
                onClick={handleArchive}
                disabled={archiving || confirming}
              >
                {archiving ? 'Архивирую...' : 'Архивировать'}
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default TopNPage;
