CREATE TABLE users (
    keycloak_id  UUID PRIMARY KEY,
    display_name VARCHAR(200) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);