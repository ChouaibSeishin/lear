package org.lear.aibotservice.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmbeddingService {

    @Value("${spring.ai.google.api-key}")
    private String apiKey;

    @Value("${gemini.embedding-model-name:text-embedding-004}")
    private String embeddingModelName;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Attempted to generate embedding for empty or null text.");
            return Collections.emptyList();
        }

        // Correct request format for Google Generative Language API v1beta
        Map<String, Object> requestBody = Map.of(
                "model", "models/" + embeddingModelName,
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text)
                        )
                )
        );

        try {
            log.debug("Sending embedding request for text: {}", text.substring(0, Math.min(text.length(), 100)) + "...");

            Map<?, ?> response = webClient.post()
                    .uri("/v1beta/models/" + embeddingModelName + ":embedContent?key=" + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(body -> {
                                log.error("Error response from embedding API: Status={}, Body={}",
                                        clientResponse.statusCode(), body);
                                return new RuntimeException("Error response from embedding API: " + body);
                            }))
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("embedding")) {
                Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
                if (embedding.containsKey("values")) {
                    List<Double> embeddingValues = (List<Double>) embedding.get("values");
                    log.debug("Successfully generated embedding with {} dimensions", embeddingValues.size());
                    return embeddingValues;
                }
            }

            log.warn("Embedding API response did not contain expected 'embedding.values' field");
            log.debug("Full response structure: {}", response);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to generate embedding for text: '{}': {}",
                    text.substring(0, Math.min(text.length(), 50)), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Generate embeddings for multiple texts in batch
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        return texts.stream()
                .map(this::generateEmbedding)
                .collect(Collectors.toList());
    }

    /**
     * Check if the embedding service is properly configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() &&
                embeddingModelName != null && !embeddingModelName.trim().isEmpty();
    }
}
