package com.example.chatbotbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public ChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getReply(String message) {
        // Use the stable v1 endpoint and ensure the key is appended as a query parameter
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        // Structured exactly as Google Gemini expects: { "contents": [{"parts": [{"text": "..."}]}] }
        Map<String, Object> textPart = Map.of("text", message);
        Map<String, Object> contentsPart = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(contentsPart));

        try {
            // Log the URL (without the key for safety) to verify the service is running
            System.out.println("Calling Gemini API for message: " + message);

            // Make the POST request
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

            // Extract response text: candidates[0] -> content -> parts[0] -> text
            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                    List<?> parts = (List<?>) content.get("parts");
                    Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);

                    return firstPart.get("text").toString();
                }
            }
            return "No response content from Gemini.";

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // This happens if the key is empty or the model name is wrong
            return "API Error: 404 Not Found. Please ensure your API key is correctly set in application.properties. " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Connection Error: " + e.getMessage();
        }
    }
}