package com.alevel.rmanager.data.exception;

public abstract class RManagerDataAccessException extends Exception {
    public RManagerDataAccessException() {
        super();
    }

    public RManagerDataAccessException(String message) {
        super(message);
    }

    public RManagerDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public RManagerDataAccessException(Throwable cause) {
        super(cause);
    }
}
