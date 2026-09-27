package io.paideia.enrollment.service.exception.custom;

import java.util.UUID;

public class CourseNotAvailableException extends RuntimeException {

    public CourseNotAvailableException(UUID courseId, String reason) {
        super("Course " + courseId + " is not available: " + reason);
    }
}
