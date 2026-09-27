package io.paideia.content.service.exception.custom;

public class InvalidContentUploadException extends RuntimeException {

    public InvalidContentUploadException(String message) {
        super(message);
    }
}
