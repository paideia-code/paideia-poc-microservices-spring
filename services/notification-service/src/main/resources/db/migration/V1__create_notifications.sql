CREATE TABLE notifications (
    id           UUID         PRIMARY KEY,
    recipient_id UUID         NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    subject      VARCHAR(255) NOT NULL,
    body         TEXT         NOT NULL,
    status       VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT notifications_status_check CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_notifications_recipient_id ON notifications (recipient_id);