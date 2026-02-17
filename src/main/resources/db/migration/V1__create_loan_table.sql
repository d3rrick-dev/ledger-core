CREATE TABLE IF NOT EXISTS loan
(
    user_id    UUID PRIMARY KEY,
    amount     NUMERIC(19, 2),
    status     VARCHAR(50),
    created_at TIMESTAMP
);