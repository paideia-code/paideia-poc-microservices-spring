CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    student_id UUID NOT NULL,
    payment_id UUID,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),


    CONSTRAINT enrollments_status_check CHECK (
            status IN (
                'REQUESTED',
                'PAYMENT_PENDING',
                'ENROLLED',
                'REJECTED'
            )
        )
    );

CREATE UNIQUE INDEX idx_enrollments_student_course_enrolled ON enrollments (student_id, course_id) WHERE status = 'ENROLLED';

CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);