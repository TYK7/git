package com.example.aiqa.controller;

import com.example.aiqa.dto.AnswerResponse;
import com.example.aiqa.dto.QuestionRequest;
import com.example.aiqa.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final AiService aiService;

    @Autowired
    public ApiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AnswerResponse> ask(@RequestBody QuestionRequest request) {
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            // Returning an error response DTO might be better, but for simplicity:
            return ResponseEntity.badRequest().body(new AnswerResponse("Question cannot be empty."));
        }

        try {
            String documentContent = aiService.readDocument();
            String answer = aiService.getAnswerFromAi(documentContent, request.getQuestion());
            return ResponseEntity.ok(new AnswerResponse(answer));
        } catch (Exception e) {
            // Specific exceptions should be handled by GlobalExceptionHandler
            // This is a fallback or for exceptions not caught by GlobalExceptionHandler
            // Log the exception e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new AnswerResponse("Error processing your request: " + e.getMessage()));
        }
    }
}
