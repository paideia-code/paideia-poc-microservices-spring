package io.paideia.content.service.exception.custom;

import java.util.UUID;

public class ContentAccessDeniedException extends RuntimeException {

    public ContentAccessDeniedException(UUID courseId) {
        super("Student has no purchased access to course " + courseId);
    }
}
