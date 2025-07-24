package org.lear.aibotservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.lear.aibotservice.models.RAGQuery;
import org.lear.aibotservice.models.RAGResponse;
import org.lear.aibotservice.services.RAGService;
import org.lear.aibotservice.services.VectorStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@Slf4j
public class RAGController {

    private final RAGService ragService;
    private final VectorStoreService vectorStoreService;

    public RAGController(RAGService ragService,VectorStoreService vectorStoreService) {
        this.ragService = ragService;
        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/ask")
    public ResponseEntity<RAGResponse> ask(@RequestBody RAGQuery query) {
        try {
            RAGResponse response = ragService.ask(query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing RAG request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RAGResponse(
                            "Internal server error occurred.",
                            Collections.emptyList(),
                            0.0,
                            "Error: " + e.getMessage()
                    ));
        }
    }

    // Simple endpoint for string queries
    @PostMapping("/ask-simple")
    public ResponseEntity<RAGResponse> askSimple(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RAGResponse(
                                "Question cannot be empty.",
                                Collections.emptyList(),
                                0.0,
                                "Invalid request: empty question"
                        ));
            }

            RAGResponse response = ragService.ask(question);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing simple RAG request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RAGResponse(
                            "Internal server error occurred.",
                            Collections.emptyList(),
                            0.0,
                            "Error: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        try {
            ragService.reindexData();
            return ResponseEntity.ok("Reindexing started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start reindexing: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/debug/vector-store")
    public String debugVectorStore() {
        vectorStoreService.logVectorStoreStats();
        return "Check logs for vector store statistics";
    }
}
