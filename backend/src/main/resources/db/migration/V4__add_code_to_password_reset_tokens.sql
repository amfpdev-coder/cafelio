ALTER TABLE password_reset_tokens
    ADD COLUMN code_hash VARCHAR(255),
    ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0;