package com.example.aiqa.dto;

public class QuestionRequest {
    private String question;

    // Default constructor (needed for JSON deserialization)
    public QuestionRequest() {
    }

    public QuestionRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
