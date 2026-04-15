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

  const [quickTopN, setQuickTopN] = useState(3);
  const [quickDestinationId, setQuickDestinationId] = useState('');
  const [quickLoading, setQuickLoading] = useState(false);
  const [quickError, setQuickError] = useState(null);
  const [quickResult, setQuickResult] = useState(null);

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

  useEffect(() => {
    if (workspaceCompanyId) {
      localStorage.setItem('lastCompanyId', String(workspaceCompanyId));
    }
    loadContext(workspaceCompanyId);
  }, [workspaceCompanyId, loadContext]);

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
    } catch (err) {
      setPubError(err.response?.data?.message ?? 'Ошибка публикации');
    } finally {
      setPublishing(false);
    }
  }

  async function handleQuickPublish(e) {
    e.preventDefault();
    setQuickError(null);
    setQuickResult(null);
    setQuickLoading(true);
    try {
      const reportResponse = await client.post(
        `/reports/top-n/request/${workspaceCompanyId}`,
        null,
        { params: { topN: Number(quickTopN) } }
      );
      const createdReport = reportResponse.data;
      const publishResponse = await client.post(
        `/reports/top-n/${createdReport.id}/publish`,
        { destinationId: Number(quickDestinationId) }
      );
      setQuickResult({
        reportId: createdReport.id,
        publication: publishResponse.data,
      });
      setPubResult(publishResponse.data);
    } catch (err) {
      setQuickError(err.response?.data?.message ?? 'Ошибка быстрого сценария');
    } finally {
      setQuickLoading(false);
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
        <h2>Опубликовать отчёт</h2>
        <form className="pub-form quick-form" onSubmit={handleQuickPublish}>
          <label>
            Размер ТОП (N)
            <input
              type="number"
              value={quickTopN}
              onChange={e => setQuickTopN(e.target.value)}
              required
              min="1"
              max="50"
            />
          </label>
          <label>
            Куда публиковать
            <select
              value={quickDestinationId}
              onChange={e => setQuickDestinationId(e.target.value)}
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
          <button
            type="submit"
            disabled={quickLoading || !workspaceCompanyId || !quickDestinationId}
          >
            {quickLoading ? 'Выполняю...' : 'Запросить TOP-N и опубликовать'}
          </button>
        </form>

        {quickError && <div className="alert alert-error">{quickError}</div>}
        {quickResult && (
          <div className="alert alert-success">
            <strong>Быстрый сценарий выполнен</strong>
            <div className="pub-result-row">
              <span>Отчёт № <b>{quickResult.reportId}</b></span>
              <span>Публикация № <b>{quickResult.publication.publicationId}</b></span>
              <StatusBadge status={quickResult.publication.status} />
            </div>
          </div>
        )}

        <hr className="sub-divider" />
        <form className="pub-form" onSubmit={handlePublish}>
          <label>
            Номер отчёта (ТОП-N)
            <input
              type="number"
              value={pubForm.reportId}
              onChange={e => setPubForm({ ...pubForm, reportId: e.target.value })}
              required
              min="1"
            />
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
              <span>№ <b>{pubResult.publicationId}</b></span>
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
                  <th>№</th>
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
                    <td>{p.publicationId}</td>
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
