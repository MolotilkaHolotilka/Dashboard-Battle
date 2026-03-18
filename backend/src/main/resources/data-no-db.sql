-- Тестовые данные для профиля no-db (H2 in-memory)
INSERT INTO users (email, password_hash, role) VALUES ('demo@test.ru', 'demo123', 'USER');
INSERT INTO companies (name, user_id) VALUES ('Демо Компания', 1);
INSERT INTO moysklad_integrations (company_id, token_encrypted, status) VALUES (1, 'test-token-123', 'ACTIVE');
INSERT INTO telegram_integrations (company_id, bot_token_encrypted, channel_chat_id, status) VALUES (1, 'test-bot-token', '-100123456', 'ACTIVE');
INSERT INTO publish_channels (code, name) VALUES ('TELEGRAM', 'Telegram');
INSERT INTO publish_destinations (channel_id, company_id, external_identifier, label) VALUES (1, 1, '-100123456', 'Тестовый тг-канал');
