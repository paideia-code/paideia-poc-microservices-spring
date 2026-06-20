CREATE TABLE users (
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    name       VARCHAR(200) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_role_check CHECK (role IN ('STUDENT', 'ADMIN'))
);

CREATE INDEX idx_users_email ON users (email);
