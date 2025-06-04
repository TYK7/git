package com.example.aiqa.exception;

import com.example.aiqa.dto.AnswerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle custom DocumentReadException
    @ExceptionHandler(DocumentReadException.class)
    public ResponseEntity<AnswerResponse> handleDocumentReadException(DocumentReadException ex, WebRequest request) {
        AnswerResponse errorResponse = new AnswerResponse("Error reading document: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle general exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AnswerResponse> handleGlobalException(Exception ex, WebRequest request) {
        AnswerResponse errorResponse = new AnswerResponse("An unexpected error occurred: " + ex.getMessage());
        // It's good practice to log the exception here
        // logger.error("Unexpected error:", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
