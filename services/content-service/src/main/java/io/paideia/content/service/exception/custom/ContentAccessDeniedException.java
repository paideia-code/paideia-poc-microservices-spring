package io.paideia.content.service.exception.custom;

import java.util.UUID;

public class ContentAccessDeniedException extends RuntimeException {

    public ContentAccessDeniedException(UUID studentId, UUID courseId) {
        super("Student " + studentId + " has no purchased access to course " + courseId);
    }
}
