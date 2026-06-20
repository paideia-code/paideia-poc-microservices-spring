package io.paideia.enrollment.service.exception.custom;

import java.util.UUID;

public class EnrollmentAlreadyExistsException extends RuntimeException {

    public EnrollmentAlreadyExistsException(UUID courseId, UUID studentId) {
        super("Student " + studentId + " is already enrolled in course " + courseId);
    }
}
