import { useState, useEffect, useCallback } from 'react';
import client from '../api/client';
import useUserCompanies from '../hooks/useUserCompanies';
import './PublicationsPage.css';

const STATUS_LABEL = {
  PUBLISHING: 'Публикуется',
  PUBLISHED: 'Опубликован',
  FAILED: 'Ошибка',
  RECALLED: 'Отозван',
};

function StatusBadge({ status }) {
  return (
    <span className={`badge badge-${status?.toLowerCase()}`}>
      {STATUS_LABEL[status] ?? status}
    </span>
  );
}

function PublicationsPage() {
  const [workspaceCompanyId, setWorkspaceCompanyId] = useState(
    () => localStorage.getItem('lastCompanyId') ?? ''
  );
  const [companyLabel, setCompanyLabel] = useState('');
  const [destinations, setDestinations] = useState([]);

  const [pubForm, setPubForm] = useState({ reportId: '', destinationId: '' });
  const [pubResult, setPubResult] = useState(null);
  const [pubError, setPubError] = useState(null);
  const [publishing, setPublishing] = useState(false);

  const [newTopN, setNewTopN] = useState(3);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [createResult, setCreateResult] = useState(null);
  const [reports, setReports] = useState([]);
  const [reportsLoading, setReportsLoading] = useState(false);

  const [publications, setPublications] = useState(null);
  const [listError, setListError] = useState(null);
  const [listLoading, setListLoading] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);
  const { companies, refreshCompanies } = useUserCompanies();

  const loadContext = useCallback(async (cid) => {
    if (!cid) {
      setCompanyLabel('');
      setDestinations([]);
      return;
    }
    try {
      const [{ data: company }, { data: dests }] = await Promise.all([
        client.get(`/companies/${cid}`),
        client.get('/publish/destinations', { params: { companyId: cid } }),
      ]);
      setCompanyLabel(company.name ?? '');
      setDestinations(dests);
    } catch {
      setCompanyLabel('');
      setDestinations([]);
    }
  }, []);

  const loadReports = useCallback(async (cid) => {
    if (!cid) {
      setReports([]);
      return;
    }
    setReportsLoading(true);
    try {
      const { data } = await client.get('/reports/top-n', { params: { companyId: cid } });
      setReports(data ?? []);
    } catch {
      setReports([]);
    } finally {
      setReportsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (workspaceCompanyId) {
      localStorage.setItem('lastCompanyId', String(workspaceCompanyId));
    }
    loadContext(workspaceCompanyId);
    loadReports(workspaceCompanyId);
  }, [workspaceCompanyId, loadContext, loadReports]);

  async function handlePublish(e) {
    e.preventDefault();
    setPubError(null);
    setPubResult(null);
    setPublishing(true);
    try {
      const { data } = await client.post(
        `/reports/top-n/${pubForm.reportId}/publish`,
        { destinationId: Number(pubForm.destinationId) }
      );
      setPubResult(data);
      await loadReports(workspaceCompanyId);
    } catch (err) {
      setPubError(err.response?.data?.message ?? 'Ошибка публикации');
    } finally {
      setPublishing(false);
    }
  }

  async function handleCreateReport(e) {
    e.preventDefault();
    setCreateError(null);
    setCreateResult(null);
    setCreateLoading(true);
    try {
      const reportResponse = await client.post(
        `/reports/top-n/request/${workspaceCompanyId}`,
        null,
        { params: { topN: Number(newTopN) } }
      );
      const createdReport = reportResponse.data;
      setCreateResult(createdReport);
      setPubForm(prev => ({ ...prev, reportId: String(createdReport.id) }));
      await loadReports(workspaceCompanyId);
    } catch (err) {
      setCreateError(err.response?.data?.message ?? 'Ошибка формирования ТОП-N');
    } finally {
      setCreateLoading(false);
    }
  }

  async function handleLoadList(e) {
    e.preventDefault();
    setListError(null);
    setPublications(null);
    setListLoading(true);
    try {
      const { data } = await client.get('/publications', {
        params: { companyId: workspaceCompanyId },
      });
      setPublications(data);
    } catch (err) {
      setListError(err.response?.data?.message ?? 'Ошибка загрузки');
    } finally {
      setListLoading(false);
    }
  }

  async function handleCancel(pubId) {
    setCancellingId(pubId);
    try {
      const { data } = await client.post(`/publications/${pubId}/cancel`);
      setPublications(prev =>
        (prev ?? []).map(p => (p.publicationId === pubId ? data : p))
      );
      await loadReports(workspaceCompanyId);
    } catch (err) {
      setListError(err.response?.data?.message ?? 'Ошибка отмены');
    } finally {
      setCancellingId(null);
    }
  }

  function demoViewerUrl(viewerPath) {
    if (!viewerPath) return null;
    return `${window.location.origin}${viewerPath.startsWith('/') ? '' : '/'}${viewerPath}`;
  }

  const destinationOptions = destinations;
  const availableReports = reports.filter(report => report.status === 'PENDING' || report.status === 'CONFIRMED');

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

  return (
    <div className="pub-page">
      <section className="pub-section workspace">
        <h2>Компания для получения списка ТОП-N сотрудников</h2>
        <div className="workspace-bar">
          <label>
            Компания
            <select
              value={workspaceCompanyId}
              onChange={e => setWorkspaceCompanyId(e.target.value)}
            >
              <option value="">— выберите компанию —</option>
              {companies.map(company => (
                <option key={company.id} value={company.id}>
                  {company.name}
                </option>
              ))}
            </select>
          </label>
          <button type="button" onClick={refreshCompanies}>
            Обновить компании
          </button>
          {companyLabel && (
            <span className="company-hint">{companyLabel}</span>
          )}
        </div>
        <p className="hint">
          Места публикации подгружаются автоматически. Настроить каналы можно в разделе «Интеграции».
        </p>
      </section>

      <section className="pub-section">
        <h2>Шаг 1. Сформировать рейтинг</h2>
        <form className="pub-form quick-form" onSubmit={handleCreateReport}>
          <label>
            Размер ТОП (N)
            <input
              type="number"
              value={newTopN}
              onChange={e => setNewTopN(e.target.value)}
              required
              min="1"
              max="50"
            />
          </label>
          <button type="submit" disabled={createLoading || !workspaceCompanyId}>
            {createLoading ? 'Формирую...' : 'Сформировать TOP-N'}
          </button>
        </form>

        {createError && <div className="alert alert-error">{createError}</div>}
        {createResult && (
          <div className="alert alert-success">
            <strong>Рейтинг сформирован</strong>
            <div className="pub-result-row">
              <span>
                {createResult.companyName ?? companyLabel ?? 'Компания'}{' '}
                ({formatDateTime(createResult.createdAt)})
              </span>
              <span>Статус: <StatusBadge status={createResult.status} /></span>
            </div>
          </div>
        )}

        <h3 className="subheading">Ранее сформированные рейтинги, доступные для публикации</h3>
        {reportsLoading ? (
          <p className="hint">Загрузка сформированных рейтингов...</p>
        ) : availableReports.length === 0 ? (
          <p className="hint">Подтверждённых или ожидающих подтверждения рейтингов пока нет.</p>
        ) : (
          <ul className="report-list">
            {availableReports.map(report => (
              <li key={report.id}>
                <button
                  type="button"
                  className={`report-pick ${String(pubForm.reportId) === String(report.id) ? 'active' : ''}`}
                  onClick={() => setPubForm(prev => ({ ...prev, reportId: String(report.id) }))}
                >
                  <span>{report.companyName ?? companyLabel ?? `Компания #${report.companyId}`}</span>
                  <span>{formatDateTime(report.createdAt)}</span>
                  <StatusBadge status={report.status} />
                </button>
              </li>
            ))}
          </ul>
        )}

        <hr className="sub-divider" />
        <h2>Шаг 2. Опубликовать рейтинг</h2>
        <form className="pub-form" onSubmit={handlePublish}>
          <label>
            Выберите рейтинг
            <select
              value={pubForm.reportId}
              onChange={e => setPubForm({ ...pubForm, reportId: e.target.value })}
              required
            >
              <option value="">— выберите рейтинг —</option>
              {availableReports.map(report => (
                <option key={report.id} value={report.id}>
                  {`${report.companyName ?? companyLabel ?? `Компания #${report.companyId}`} · ${formatDateTime(report.createdAt)}`}
                </option>
              ))}
            </select>
          </label>
          <label>
            Место публикации
            <select
              value={pubForm.destinationId}
              onChange={e => setPubForm({ ...pubForm, destinationId: e.target.value })}
              required
            >
              <option value="">— выберите —</option>
              {destinationOptions.map(d => (
                <option key={d.id} value={d.id}>
                  {d.label} ({d.channelName})
                </option>
              ))}
            </select>
          </label>
          <button type="submit" disabled={publishing}>
            {publishing ? 'Публикую...' : 'Опубликовать'}
          </button>
        </form>

        {pubError && <div className="alert alert-error">{pubError}</div>}

        {pubResult && (
          <div className="alert alert-success">
            <strong>Публикация создана</strong>
            <div className="pub-result-row">
              <span>{formatDateTime(pubResult.createdAt)}</span>
              <span>{pubResult.companyName ?? companyLabel ?? 'Компания'}</span>
              <span>Канал: <b>{pubResult.channelName ?? pubResult.channelId}</b></span>
              <span>Место: <b>{pubResult.destinationLabel ?? pubResult.destinationId}</b></span>
              <StatusBadge status={pubResult.status} />
            </div>
            {pubResult.externalId && (
              <div className="ext-id">Внешний идентификатор: {pubResult.externalId}</div>
            )}
            {pubResult.viewerPath && (
              <div className="demo-link">
                <a href={demoViewerUrl(pubResult.viewerPath)} target="_blank" rel="noreferrer">
                  Открыть демо-страницу
                </a>
              </div>
            )}
          </div>
        )}
      </section>

      <hr className="divider" />

      <section className="pub-section">
        <h2>История публикаций</h2>
        <form className="list-form" onSubmit={handleLoadList}>
          <span className="list-hint">Текущая компания: <b>{workspaceCompanyId || '—'}</b></span>
          <button type="submit" disabled={listLoading || !workspaceCompanyId}>
            {listLoading ? 'Загрузка...' : 'Загрузить историю'}
          </button>
        </form>

        {listError && <div className="alert alert-error">{listError}</div>}

        {publications !== null && (
          publications.length === 0 ? (
            <p className="empty">Публикаций пока нет</p>
          ) : (
            <table className="pub-table">
              <thead>
                <tr>
                  <th>Дата</th>
                  <th>Рейтинг</th>
                  <th>Канал</th>
                  <th>Место</th>
                  <th>Статус</th>
                  <th>Внешний ID</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {publications.map(p => (
                  <tr key={p.publicationId}>
                    <td>{formatDateTime(p.createdAt)}</td>
                    <td>{`${p.companyName ?? companyLabel ?? 'Компания'} · #${p.reportId ?? '—'}`}</td>
                    <td>{p.channelName ?? p.channelCode ?? p.channelId}</td>
                    <td>{p.destinationLabel ?? p.destinationId}</td>
                    <td><StatusBadge status={p.status} /></td>
                    <td className="ext">{p.externalId ?? '—'}</td>
                    <td>
                      {p.status === 'PUBLISHED' && p.channelCode === 'TELEGRAM' && (
                        <button
                          className="btn-cancel"
                          type="button"
                          onClick={() => handleCancel(p.publicationId)}
                          disabled={cancellingId === p.publicationId}
                        >
                          {cancellingId === p.publicationId ? '...' : 'Отменить'}
                        </button>
                      )}
                      {p.status === 'PUBLISHED' && p.channelCode === 'DEMO_PAGE' && p.viewerPath && (
                        <a
                          className="btn-link"
                          href={demoViewerUrl(p.viewerPath)}
                          target="_blank"
                          rel="noreferrer"
                        >
                          Смотреть
                        </a>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )
        )}
      </section>
    </div>
  );
}

export default PublicationsPage;
