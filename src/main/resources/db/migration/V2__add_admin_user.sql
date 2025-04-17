INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('Admin User', 'admin@example.com', '$2b$12$XVt2H1x1s8xIqve4Zj4d3uHn.Px83wI9zfCf.GI6thPcQ7XMT9yda', 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;