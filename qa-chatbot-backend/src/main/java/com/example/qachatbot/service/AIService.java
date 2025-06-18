package com.example.qachatbot.service;

public interface AIService {
    /**
     * Generates an answer based on the provided document content and question.
     *
     * @param documentContent The content of the document to search within.
     * @param question The user's question.
     * @return The generated answer, or a message indicating the question is out of scope.
     */
    String getAnswer(String documentContent, String question);
}
