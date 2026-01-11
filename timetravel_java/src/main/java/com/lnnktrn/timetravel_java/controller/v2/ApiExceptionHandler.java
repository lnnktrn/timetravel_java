package com.lnnktrn.timetravel_java.controller.v2;

import com.lnnktrn.timetravel_java.exception.NoSuchRecordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchRecordException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchRecord(NoSuchRecordException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "NOT_FOUND",
                        ex.getMessage()
                ));
    }
}