INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('Admin User', 'admin@example.com', '$2a$10$eDIJU8w8WNeR2ewj.8FKEuQDe7chinH7YrNfphUZkqB1ShDr1hDQC', 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;