INSERT INTO users (id, username, password, role, is_active, balance)
VALUES (1, 'subuser', 'hash123', 'USER', true, 500.00);

INSERT INTO subscriptions (id, name, price, duration_days, is_free_start, include_minutes)
VALUES (1, 'PRO-подписка', 299.00, 30, true, 0);
