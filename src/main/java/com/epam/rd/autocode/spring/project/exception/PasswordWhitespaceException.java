package com.epam.rd.autocode.spring.project.exception;

public class PasswordWhitespaceException extends RuntimeException {
    public PasswordWhitespaceException(String message) {
        super(message);
    }
}
