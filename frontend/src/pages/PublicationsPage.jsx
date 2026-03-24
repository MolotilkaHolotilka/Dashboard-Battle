import { useState } from 'react';
import client from '../api/client';
import './PublicationsPage.css';

const STATUS_LABEL = {
  PUBLISHING: 'Публикуется',
  PUBLISHED:  'Опубликован',
  FAILED:     'Ошибка',
  RECALLED:   'Отозван',
};

function StatusBadge({ status }) {
  return (
    <span className={`badge badge-${status?.toLowerCase()}`}>
      {STATUS_LABEL[status] ?? status}
    </span>
  );
}

function PublicationsPage() {
  // --- publish form ---
  const [pubForm, setPubForm] = useState({ reportId: '', destinationId: '' });
  const [pubResult, setPubResult] = useState(null);
  const [pubError, setPubError] = useState(null);
  const [publishing, setPublishing] = useState(false);

  // --- list form ---
  const [companyId, setCompanyId] = useState('');
  const [publications, setPublications] = useState(null);
  const [listError, setListError] = useState(null);
  const [listLoading, setListLoading] = useState(false);

  // --- cancel ---
  const [cancellingId, setCancellingId] = useState(null);

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

  async function handleLoadList(e) {
    e.preventDefault();
    setListError(null);
    setPublications(null);
    setListLoading(true);
    try {
      const { data } = await client.get('/publications', { params: { companyId } });
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
        prev.map(p => (p.publicationId === pubId ? data : p))
      );
    } catch (err) {
      setListError(err.response?.data?.message ?? 'Ошибка отмены');
    } finally {
      setCancellingId(null);
    }
  }

  return (
    <div className="pub-page">

      {/* ── Опубликовать ── */}
      <section className="pub-section">
        <h2>Опубликовать отчёт</h2>
        <form className="pub-form" onSubmit={handlePublish}>
          <label>
            ID отчёта (ТОП-N)
            <input
              type="number"
              value={pubForm.reportId}
              onChange={e => setPubForm({ ...pubForm, reportId: e.target.value })}
              placeholder="1"
              required
              min="1"
            />
          </label>
          <label>
            ID места назначения
            <input
              type="number"
              value={pubForm.destinationId}
              onChange={e => setPubForm({ ...pubForm, destinationId: e.target.value })}
              placeholder="1"
              required
              min="1"
            />
          </label>
          <button type="submit" disabled={publishing}>
            {publishing ? 'Публикую...' : 'Опубликовать'}
          </button>
        </form>

        {pubError && <div className="alert alert-error">{pubError}</div>}

        {pubResult && (
          <div className="alert alert-success">
            <strong>Публикация создана!</strong>
            <div className="pub-result-row">
              <span>ID: <b>{pubResult.publicationId}</b></span>
              <span>Канал: <b>{pubResult.channelId}</b></span>
              <span>Destination: <b>{pubResult.destinationId}</b></span>
              <StatusBadge status={pubResult.status} />
            </div>
            {pubResult.externalId && (
              <div className="ext-id">External ID: {pubResult.externalId}</div>
            )}
          </div>
        )}
      </section>

      <hr className="divider" />

      {/* ── История публикаций ── */}
      <section className="pub-section">
        <h2>История публикаций</h2>
        <form className="list-form" onSubmit={handleLoadList}>
          <label>
            ID компании
            <input
              type="number"
              value={companyId}
              onChange={e => setCompanyId(e.target.value)}
              placeholder="1"
              required
              min="1"
            />
          </label>
          <button type="submit" disabled={listLoading}>
            {listLoading ? 'Загрузка...' : 'Загрузить'}
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
                  <th>ID</th>
                  <th>Канал</th>
                  <th>Destination</th>
                  <th>Статус</th>
                  <th>External ID</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {publications.map(p => (
                  <tr key={p.publicationId}>
                    <td>{p.publicationId}</td>
                    <td>{p.channelId}</td>
                    <td>{p.destinationId}</td>
                    <td><StatusBadge status={p.status} /></td>
                    <td className="ext">{p.externalId ?? '—'}</td>
                    <td>
                      {p.status === 'PUBLISHED' && (
                        <button
                          className="btn-cancel"
                          onClick={() => handleCancel(p.publicationId)}
                          disabled={cancellingId === p.publicationId}
                        >
                          {cancellingId === p.publicationId ? '...' : 'Отменить'}
                        </button>
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
