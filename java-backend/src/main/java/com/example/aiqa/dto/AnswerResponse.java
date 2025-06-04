package com.example.aiqa.dto;

public class AnswerResponse {
    private String answer;

    // Default constructor (needed for JSON deserialization)
    public AnswerResponse() {
    }

    public AnswerResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
