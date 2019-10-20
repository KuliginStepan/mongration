package com.kuliginstepan.mongration;

public class MongrationException extends RuntimeException {

    public MongrationException(String message) {
        super(message);
    }

    public MongrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
