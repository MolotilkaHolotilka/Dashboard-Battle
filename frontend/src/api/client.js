import axios from 'axios';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const moyskladToken = localStorage.getItem('debug.moyskladToken');
  const telegramToken = localStorage.getItem('debug.telegramToken');
  const telegramChatId = localStorage.getItem('debug.telegramChatId');

  if (moyskladToken) {
    config.headers['X-Debug-Moysklad-Token'] = moyskladToken;
  }
  if (telegramToken) {
    config.headers['X-Debug-Telegram-Token'] = telegramToken;
  }
  if (telegramChatId) {
    config.headers['X-Debug-Telegram-Chat-Id'] = telegramChatId;
  }
  return config;
});

export default client;
