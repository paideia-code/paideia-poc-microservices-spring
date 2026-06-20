package io.paideia.course.service.exception.custom;

import java.util.UUID;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(UUID id) {
        super("Course not found: " + id);
    }
}
