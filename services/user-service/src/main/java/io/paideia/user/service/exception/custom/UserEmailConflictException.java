package io.paideia.user.service.exception.custom;

public class UserEmailConflictException extends RuntimeException {

    public UserEmailConflictException(String email) {
        super("A user with email '" + email + "' already exists");
    }
}
