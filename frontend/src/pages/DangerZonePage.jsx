import { useState } from 'react';
import './DangerZonePage.css';

function DangerZonePage() {
  const [moyskladToken, setMoyskladToken] = useState(localStorage.getItem('debug.moyskladToken') ?? '');
  const [telegramToken, setTelegramToken] = useState(localStorage.getItem('debug.telegramToken') ?? '');
  const [telegramChatId, setTelegramChatId] = useState(localStorage.getItem('debug.telegramChatId') ?? '');
  const [saved, setSaved] = useState(false);

  function handleSave(e) {
    e.preventDefault();
    localStorage.setItem('debug.moyskladToken', moyskladToken.trim());
    localStorage.setItem('debug.telegramToken', telegramToken.trim());
    localStorage.setItem('debug.telegramChatId', telegramChatId.trim());
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  function handleClear() {
    localStorage.removeItem('debug.moyskladToken');
    localStorage.removeItem('debug.telegramToken');
    localStorage.removeItem('debug.telegramChatId');
    setMoyskladToken('');
    setTelegramToken('');
    setTelegramChatId('');
    setSaved(false);
  }

  return (
    <div className="danger-page">
      <h2>Danger Zone Debug</h2>
      <div className="danger-warning">
        Небезопасно: токены хранятся в браузере и отправляются заголовками на API.
        Для нормального режима используйте `.env`.
      </div>

      <form className="danger-form" onSubmit={handleSave}>
        <label>
          MoySklad token (debug)
          <textarea
            value={moyskladToken}
            onChange={(e) => setMoyskladToken(e.target.value)}
            placeholder="Вставьте токен МойСклад"
            rows={3}
          />
        </label>
        <label>
          Telegram bot token (debug)
          <textarea
            value={telegramToken}
            onChange={(e) => setTelegramToken(e.target.value)}
            placeholder="Вставьте токен бота"
            rows={2}
          />
        </label>
        <label>
          Telegram chat id (debug)
          <input
            type="text"
            value={telegramChatId}
            onChange={(e) => setTelegramChatId(e.target.value)}
            placeholder="-100..."
          />
        </label>
        <div className="danger-actions">
          <button type="submit">Сохранить в браузере</button>
          <button type="button" className="secondary" onClick={handleClear}>Очистить</button>
        </div>
      </form>

      {saved && <div className="danger-saved">Сохранено. Новые запросы пойдут с debug-заголовками.</div>}
    </div>
  );
}

export default DangerZonePage;
