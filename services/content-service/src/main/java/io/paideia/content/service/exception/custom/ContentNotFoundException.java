package io.paideia.content.service.exception.custom;

import java.util.UUID;

public class ContentNotFoundException extends RuntimeException{

    public ContentNotFoundException(UUID contentId) {
        super("Content not found: " + contentId);
    }
    
}
