package com.zsoft.gloody.gloodypayroll.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ApiErrorResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Date timestamp;
    private HttpStatus status;
    private String message;
    private List<String> errors;

    public ApiErrorResponse(HttpStatus status, String message, List<String> errors) {
        this.timestamp = new Date();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiErrorResponse(HttpStatus status, String message, String error) {
        this.timestamp = new Date();
        this.status = status;
        this.message = message;
        errors = Collections.singletonList(error);
    }
}
