package com.dianping.trek.exception;

public class InvalidMessageException extends RuntimeException {

    private static final long serialVersionUID = 3706036581524408336L;
    
    public InvalidMessageException() {
    }

    public InvalidMessageException(String message) {
        super(message);
    }

    public InvalidMessageException(Throwable cause) {
        super(cause);
    }

    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
