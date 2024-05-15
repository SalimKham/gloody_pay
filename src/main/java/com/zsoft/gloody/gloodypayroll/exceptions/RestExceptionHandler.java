package com.zsoft.gloody.gloodypayroll.exceptions;

import io.jsonwebtoken.ClaimJwtException;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private final Environment environment;

    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request
    ) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + '.' + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + '.' + error.getDefaultMessage());
        }
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, response, headers, response.getStatus(), request);
    }

    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request
    ) {
        String error = ex.getParameterName() + " parameter is missing";
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(@NonNull MethodArgumentTypeMismatchException ex) {
        String error = ex.getName() + " should be of type " + Objects.requireNonNull(ex.getRequiredType()).getName();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

    @ExceptionHandler(ApiRuntimeException.class)
    public ResponseEntity<Object> apiException(ApiRuntimeException ex) {
        String error = ex.getMessage();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

    @ExceptionHandler({AccessDeniedException.class, ClaimJwtException.class, AuthenticationException.class})
    public final ResponseEntity<Object> handleAccessDeniedException(RuntimeException ex) {
        String error = ex.getMessage();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.UNAUTHORIZED, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

 /*   @ExceptionHandler(MaxUploadSizeExceededException.class)
    public final ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String message = "file.upload.max-size";
        String error = String.format("max-size: %s", ex.getMaxUploadSize());
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, message, error);
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }*/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception ex) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("develop")) {
            ex.printStackTrace();
        }
        String error = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }
}
