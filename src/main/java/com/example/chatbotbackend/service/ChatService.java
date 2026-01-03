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
        String model = "gemini-2.5-flash-lite";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + geminiApiKey;

        Map<String, Object> textPart = Map.of("text", message);
        Map<String, Object> contentsPart = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(contentsPart));

        try {
            System.out.println("Calling Gemini API for message: " + message);

            Map<String, Object> response =
                    restTemplate.postForObject(url, requestBody, Map.class);

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
            return "API Error: 404 Not Found. Check model name and API key. " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Connection Error: " + e.getMessage();
        }
    }

}