-- Демо-данные для запуска в docker (PostgreSQL)
-- Цель: чтобы базовые эндпоинты работали сразу после docker compose up.

-- Пользователь + компания
INSERT INTO users (id, email, password_hash, role, created_at)
VALUES (1, 'demo@test.ru', 'demo123', 'USER', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO companies (id, name, user_id, created_at)
VALUES (1, 'Demo Company', 1, now())
ON CONFLICT (id) DO NOTHING;

-- Интеграции (МойСклад + Telegram)
INSERT INTO moysklad_integrations (id, company_id, token_encrypted, status, created_at)
VALUES (1, 1, 'demo-token-123', 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO telegram_integrations (id, company_id, bot_token_encrypted, channel_chat_id, status, created_at)
VALUES (1, 1, 'demo-bot-token', '-100123456', 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

-- Destination: конкретное место публикации внутри Telegram
-- publish_channels создаётся Liquibase-ом, поэтому берём channel_id по code='TELEGRAM'
INSERT INTO publish_destinations (id, channel_id, company_id, external_identifier, label, created_at)
SELECT 1, pc.id, 1, '-100123456', 'Demo tg destination', now()
FROM publish_channels pc
WHERE pc.code = 'TELEGRAM'
ON CONFLICT (id) DO NOTHING;

-- Подкрутка sequence, чтобы автоинкременты дальше не конфликтовали
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('companies', 'id'), COALESCE((SELECT MAX(id) FROM companies), 1), true);
SELECT setval(pg_get_serial_sequence('moysklad_integrations', 'id'), COALESCE((SELECT MAX(id) FROM moysklad_integrations), 1), true);
SELECT setval(pg_get_serial_sequence('telegram_integrations', 'id'), COALESCE((SELECT MAX(id) FROM telegram_integrations), 1), true);
SELECT setval(pg_get_serial_sequence('publish_destinations', 'id'), COALESCE((SELECT MAX(id) FROM publish_destinations), 1), true);

