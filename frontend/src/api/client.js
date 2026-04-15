import axios from 'axios';
import { dangerZoneEnabled } from '../config';
import { getToken, clearAuth } from '../session';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }

  if (dangerZoneEnabled) {
    const moyskladToken = localStorage.getItem('debug.moyskladToken');
    const telegramToken = localStorage.getItem('debug.telegramToken');
    const telegramChatId = localStorage.getItem('debug.telegramChatId');
    if (moyskladToken) config.headers['X-Debug-Moysklad-Token'] = moyskladToken;
    if (telegramToken) config.headers['X-Debug-Telegram-Token'] = telegramToken;
    if (telegramChatId) config.headers['X-Debug-Telegram-Chat-Id'] = telegramChatId;
  }

  return config;
});

client.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      clearAuth();
      window.dispatchEvent(new Event('auth:logout'));
    }
    return Promise.reject(error);
  }
);

export default client;
