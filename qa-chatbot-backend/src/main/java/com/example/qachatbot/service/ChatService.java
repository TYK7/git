package com.example.qachatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private final DocumentService documentService;
    private final AIService aiService;

    @Autowired
    public ChatService(DocumentService documentService, @Qualifier("mockAIService") AIService aiService) {
        this.documentService = documentService;
        this.aiService = aiService;
    }

    public String getResponse(String question) {
        LOGGER.info("ChatService: Received question: '{}'", question);
        String documentContent = documentService.getDocumentContent();

        if (documentContent == null || documentContent.startsWith("Error:")) {
            LOGGER.error("ChatService: Document content is not available or contains an error. Content: {}", documentContent);
            return "Sorry, I am currently unable to access the document content to answer your question.";
        }

        if (documentContent.trim().isEmpty()) {
            LOGGER.warn("ChatService: Document content is empty.");
            return "Sorry, the document content is empty, so I cannot answer any questions.";
        }

        // Here, you could add logic to construct a more complex prompt if needed,
        // for example, by adding instructions to the AI.
        // For the mock service, we pass the raw content and question.
        String answer = aiService.getAnswer(documentContent, question);
        LOGGER.info("ChatService: Answer from AIService: '{}'", answer);
        return answer;
    }
}
