CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    student_id UUID NOT NULL,
    payment_id UUID,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT enrollments_student_course_unique UNIQUE (student_id, course_id),
    CONSTRAINT enrollments_status_check CHECK (
        status IN (
            'REQUESTED',
            'PAYMENT_PENDING',
            'ENROLLED',
            'REJECTED',
        )
    )
);

CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);