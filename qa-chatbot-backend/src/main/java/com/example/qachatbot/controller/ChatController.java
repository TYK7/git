package com.example.qachatbot.controller;

import com.example.qachatbot.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/chat") // Changed from /api/ask to /api/chat for the controller path
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // DTO for the question
    // Using a static inner class for simplicity here. Could be a separate file.
    public static class AskRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    @PostMapping("/ask") // Endpoint will be /api/chat/ask
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody AskRequest request) {
        String question = request.getQuestion();
        LOGGER.info("ChatController: Received API request to /ask with question: '{}'", question);
        if (question == null || question.trim().isEmpty()) {
            LOGGER.warn("ChatController: Question is null or empty.");
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Question cannot be empty."));
        }

        String answer = chatService.getResponse(question);
        LOGGER.info("ChatController: Sending response: '{}'", answer);
        return ResponseEntity.ok(Collections.singletonMap("answer", answer));
    }
}
