package com.dianping.trek.exception;

public class InvalidApplicationException extends RuntimeException {

    private static final long serialVersionUID = -8040658117895332359L;

    public InvalidApplicationException() {
    }

    public InvalidApplicationException(String message) {
        super(message);
    }

    public InvalidApplicationException(Throwable cause) {
        super(cause);
    }

    public InvalidApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
