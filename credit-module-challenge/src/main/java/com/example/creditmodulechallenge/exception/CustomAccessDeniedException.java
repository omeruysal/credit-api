package com.example.creditmodulechallenge.exception;

public class CustomAccessDeniedException extends RuntimeException {

    public CustomAccessDeniedException(final String message) {
        super(message);
    }
}
