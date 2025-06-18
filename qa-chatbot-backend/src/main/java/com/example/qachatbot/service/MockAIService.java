package com.example.qachatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;

@Service("mockAIService") // Giving it a qualifier name
public class MockAIService implements AIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockAIService.class);
    private static final String OUT_OF_SCOPE_MESSAGE = "Sorry, this question is out of the scope of the provided document.";

    @Override
    public String getAnswer(String documentContent, String question) {
        LOGGER.info("MockAIService: Received question: '{}'", question);
        if (documentContent == null || documentContent.trim().isEmpty()) {
            LOGGER.warn("MockAIService: Document content is empty or null.");
            return "Sorry, there is no document content available to answer questions.";
        }

        // Simple keyword matching logic for the mock service.
        // This is a very basic example. Real AI would be much more sophisticated.
        String[] questionWords = question.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\s]", "").split("\s+");

        // Try to find sentences or paragraphs containing keywords.
        // For simplicity, let's find the first sentence that contains any of the significant question words.
        // A more advanced mock might try to find multiple relevant sentences.

        String bestMatchSentence = null;
        int maxMatches = 0;

        // Split document into sentences (simple split by period, not perfect)
        String[] sentences = documentContent.split("\.");

        for (String sentence : sentences) {
            String lowerSentence = sentence.toLowerCase(Locale.ROOT);
            int currentMatches = 0;
            for (String word : questionWords) {
                if (word.length() < 3) continue; // Ignore very short words
                if (lowerSentence.contains(word)) {
                    currentMatches++;
                }
            }
            if (currentMatches > 0 && currentMatches > maxMatches) {
                maxMatches = currentMatches;
                bestMatchSentence = sentence.trim();
            }
        }

        if (bestMatchSentence != null) {
            LOGGER.info("MockAIService: Found a matching sentence: '{}'", bestMatchSentence);
            return bestMatchSentence + "."; // Add back the period
        } else {
            // A slightly more advanced check: does any word from the question appear in the document?
            boolean keywordFound = Arrays.stream(questionWords)
                                         .filter(word -> word.length() >= 3)
                                         .anyMatch(word -> documentContent.toLowerCase(Locale.ROOT).contains(word));
            if(keywordFound) {
                 LOGGER.info("MockAIService: Keywords found, but no specific sentence strongly matched. Providing a generic relevant part (first 200 chars).");
                 // This part is tricky for a mock. A real AI would summarize or generate.
                 // For now, if keywords are present but no good sentence, still might be out of scope for a direct answer.
                 // Or, return a snippet. Let's stick to "out of scope" if no sentence is a good match.
                 // A better mock might return "I found some information related to your keywords, but couldn't form a precise answer."
                 return OUT_OF_SCOPE_MESSAGE + " (Keywords found, but no direct answer located)";
            } else {
                LOGGER.info("MockAIService: No relevant keywords found in the document for the question.");
                return OUT_OF_SCOPE_MESSAGE;
            }
        }
    }
}
