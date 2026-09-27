CREATE TABLE courses (
    id            UUID           PRIMARY KEY,
    title         VARCHAR(200)   NOT NULL UNIQUE,
    description   TEXT,
    price  NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(30)    NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT courses_price_non_negative CHECK (price >= 0),
    CONSTRAINT courses_status_check CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);