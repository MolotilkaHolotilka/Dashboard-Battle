import { useState, useEffect } from 'react';
import client from '../api/client';
import './IntegrationsPage.css';

function IntegrationsPage() {
  const [companyId, setCompanyId] = useState(() => localStorage.getItem('lastCompanyId') ?? '');
  const [moyskladToken, setMoyskladToken] = useState('');
  const [telegramBot, setTelegramBot] = useState('');
  const [telegramChat, setTelegramChat] = useState('');
  const [destLabel, setDestLabel] = useState('');
  const [destChannel, setDestChannel] = useState('TELEGRAM');
  const [destExtra, setDestExtra] = useState('');
  const [channels, setChannels] = useState([]);
  const [integrationsInfo, setIntegrationsInfo] = useState(null);
  const [destinations, setDestinations] = useState([]);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function refreshLists() {
    if (!companyId) return;
    setError(null);
    try {
      const [ch, dest, integ] = await Promise.all([
        client.get('/publish/channels'),
        client.get('/publish/destinations', { params: { companyId } }),
        client.get(`/integrations/${companyId}`),
      ]);
      setChannels(ch.data);
      setDestinations(dest.data);
      setIntegrationsInfo(integ.data);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Не удалось загрузить данные');
    }
  }

  useEffect(() => {
    client.get('/publish/channels').then(r => setChannels(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (companyId) refreshLists();
  }, [companyId]);

  async function saveMoysklad(e) {
    e.preventDefault();
    setMessage(null);
    setError(null);
    setLoading(true);
    try {
      await client.post('/integrations/moysklad', {
        companyId: Number(companyId),
        accessToken: moyskladToken.trim(),
      });
      setMoyskladToken('');
      setMessage('Интеграция МойСклад сохранена.');
      await refreshLists();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка сохранения');
    } finally {
      setLoading(false);
    }
  }

  async function saveTelegram(e) {
    e.preventDefault();
    setMessage(null);
    setError(null);
    setLoading(true);
    try {
      await client.post('/integrations/telegram', {
        companyId: Number(companyId),
        botToken: telegramBot.trim(),
        channelChatId: telegramChat.trim(),
      });
      setTelegramBot('');
      setTelegramChat('');
      setMessage('Интеграция Telegram сохранена.');
      await refreshLists();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка сохранения');
    } finally {
      setLoading(false);
    }
  }

  async function createDestination(e) {
    e.preventDefault();
    setMessage(null);
    setError(null);
    setLoading(true);
    try {
      await client.post('/publish/destinations', {
        companyId: Number(companyId),
        channelCode: destChannel,
        label: destLabel.trim() || undefined,
        externalIdentifier: destExtra.trim() || undefined,
      });
      setDestLabel('');
      setDestExtra('');
      setMessage('Место публикации создано.');
      await refreshLists();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Ошибка создания');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="integrations-page">
      <h2>Интеграции и каналы публикации</h2>
      <p className="intro">
        Укажите ID компании (после регистрации он показывается на главной и сохраняется в браузере).
        Токены уходят на сервер по HTTPS и не отображаются повторно в ответах API.
      </p>

      <label className="company-bar">
        ID компании
        <input
          type="number"
          value={companyId}
          onChange={e => setCompanyId(e.target.value)}
          min="1"
          required
        />
        <button type="button" onClick={refreshLists} disabled={!companyId || loading}>
          Обновить списки
        </button>
      </label>

      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-success">{message}</div>}

      {integrationsInfo && (
        <section className="panel muted">
          <h3>Состояние (без секретов)</h3>
          <p>МойСклад: записей {integrationsInfo.moySkladIntegrations?.length ?? 0}</p>
          <p>Telegram: записей {integrationsInfo.telegramIntegrations?.length ?? 0}</p>
        </section>
      )}

      <div className="grid-two">
        <section className="panel">
          <h3>МойСклад</h3>
          <form onSubmit={saveMoysklad}>
            <label>
              Токен доступа JSON API
              <textarea
                value={moyskladToken}
                onChange={e => setMoyskladToken(e.target.value)}
                rows={3}
                required
                placeholder="Вставьте токен из личного кабинета МойСклад"
              />
            </label>
            <button type="submit" disabled={loading || !companyId}>Сохранить</button>
          </form>
        </section>

        <section className="panel">
          <h3>Telegram</h3>
          <form onSubmit={saveTelegram}>
            <label>
              Токен бота
              <textarea
                value={telegramBot}
                onChange={e => setTelegramBot(e.target.value)}
                rows={2}
                required
              />
            </label>
            <label>
              ID канала (chat_id)
              <input value={telegramChat} onChange={e => setTelegramChat(e.target.value)} required placeholder="-100..." />
            </label>
            <button type="submit" disabled={loading || !companyId}>Сохранить</button>
          </form>
        </section>
      </div>

      <section className="panel">
        <h3>Новое место публикации</h3>
        <form className="dest-form" onSubmit={createDestination}>
          <label>
            Канал
            <select value={destChannel} onChange={e => setDestChannel(e.target.value)}>
              {channels.map(c => (
                <option key={c.code} value={c.code}>
                  {c.name} ({c.code})
                </option>
              ))}
            </select>
          </label>
          <label>
            Название для списка
            <input value={destLabel} onChange={e => setDestLabel(e.target.value)} placeholder="Например: Канал для конкурса" />
          </label>
          <label>
            Дополнительно: chat_id для Telegram / URL для Webhook
            <input
              value={destExtra}
              onChange={e => setDestExtra(e.target.value)}
              placeholder="Необязательно для Telegram, если chat_id уже в интеграции"
            />
          </label>
          <button type="submit" disabled={loading || !companyId}>Создать</button>
        </form>
      </section>

      {destinations.length > 0 && (
        <section className="panel">
          <h3>Ваши места публикации</h3>
          <ul className="dest-list">
            {destinations.map(d => (
              <li key={d.id}>
                <strong>{d.label}</strong> — {d.channelName}{' '}
                <span className="hint-code">#{d.id}</span>
                {d.externalIdentifier && (
                  <span className="hint-extra"> · {d.externalIdentifier}</span>
                )}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}

export default IntegrationsPage;
