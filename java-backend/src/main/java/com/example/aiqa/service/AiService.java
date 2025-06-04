package com.example.aiqa.service;

import com.example.aiqa.exception.DocumentReadException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final String DOCUMENT_PATH = "document.txt";

    /**
     * Reads the content of document.txt from the classpath.
     *
     * @return The content of the document as a String.
     * @throws DocumentReadException if the document cannot be read.
     */
    public String readDocument() {
        try (InputStream inputStream = new ClassPathResource(DOCUMENT_PATH).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            if (content.trim().isEmpty()) {
                // Handle case where document is empty, if necessary
                // For now, let's assume an empty document is not an error unless it's missing
            }
            return content;
        } catch (IOException e) {
            throw new DocumentReadException("Failed to read document: " + DOCUMENT_PATH, e);
        }
    }

    /**
     * Placeholder for AI interaction logic.
     * TODO: Replace this with actual Eden AI API call.
     *
     * @param documentContent The content of the document.
     * @param question        The user's question.
     * @return A mock AI response.
     */
    public String getAnswerFromAi(String documentContent, String question) {
        // TODO: Replace this with actual Eden AI API call
        // For now, this is a mock response.
        // You might want to do some basic analysis on documentContent if it helps the mock.
        if (documentContent != null && documentContent.toLowerCase().contains("sample")) {
            return "Mock AI Response: The document mentions 'sample' and your question was: " + question;
        }
        return "Mock AI Response: The document was processed and your question was: " + question;
    }
}
