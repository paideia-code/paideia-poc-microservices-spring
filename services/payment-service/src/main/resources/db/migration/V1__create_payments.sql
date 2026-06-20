CREATE TABLE payments (
    id             UUID           PRIMARY KEY,
    amount         NUMERIC(12, 2) NOT NULL,
    status         VARCHAR(30)    NOT NULL,
    failure_reason VARCHAR(100),
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT payments_amount_positive CHECK (amount > 0),

    CONSTRAINT payments_status_check CHECK (
        status IN ('APPROVED', 'REJECTED')
    ),

    CONSTRAINT payments_failure_reason_check CHECK (
        (status = 'APPROVED' AND failure_reason IS NULL)
        OR
        (status = 'REJECTED' AND failure_reason IS NOT NULL)
    ),

    CONSTRAINT payments_failure_reason_value_check CHECK (
        failure_reason IS NULL
        OR failure_reason IN (
            'PAYMENT_SIMULATION_REJECTED'
        )
    )
);