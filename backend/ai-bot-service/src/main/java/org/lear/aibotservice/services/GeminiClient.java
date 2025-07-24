package org.lear.aibotservice.services;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    @Value("${spring.ai.google.api-key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String ask(String userMessage) {
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", userMessage
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gemini-2.5-flash",
                "messages", List.of(message)
        );

        Map<?,?> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).map(body ->
                                new RuntimeException("Error response: " + body)
                        ))
                .bodyToMono(Map.class)
                .block();

        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
            return (String) messageResponse.get("content");
        } catch (Exception e) {
            return "Error parsing Gemini response.";
        }
    }
}
