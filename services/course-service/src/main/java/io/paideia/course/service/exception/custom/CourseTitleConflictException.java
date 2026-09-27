package io.paideia.course.service.exception.custom;

public class CourseTitleConflictException extends RuntimeException {

    public CourseTitleConflictException(String title) {
        super("A course with title '" + title + "' already exists");
    }
}
