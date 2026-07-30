package com.maleyk.flow_manager.handler;

import com.maleyk.flow_manager.exception.FileNotReadyException;
import com.maleyk.flow_manager.exception.FileRecordNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(FileRecordNotFoundException.class)
    public ResponseEntity<String> handleNotFound(FileRecordNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(FileNotReadyException.class)
    public ResponseEntity<String> handleNotReady(FileNotReadyException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
