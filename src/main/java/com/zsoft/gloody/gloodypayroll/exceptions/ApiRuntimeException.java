package com.zsoft.gloody.gloodypayroll.exceptions;

public class ApiRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ApiRuntimeException(String message) {
        super(message);
    }

    public ApiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
