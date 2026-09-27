package io.paideia.enrollment.service.exception.custom;

import java.util.UUID;

public class EnrollmentNotFoundException extends RuntimeException {

    public EnrollmentNotFoundException(UUID id) {
        super("Enrollment not found: " + id);
    }
}
