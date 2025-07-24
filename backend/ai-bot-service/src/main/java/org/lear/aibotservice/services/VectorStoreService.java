package org.lear.aibotservice.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lear.aibotservice.models.DocumentEmbedding;
import org.lear.aibotservice.models.RetrievedDocument;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate; // Import Predicate for filtering
import java.util.stream.Collectors;

@Service
@Slf4j
public class VectorStoreService {

    private final List<DocumentEmbedding> embeddings = new CopyOnWriteArrayList<>();
    @Getter
    private final GeminiClient geminiClient;
    private final EmbeddingService embeddingService;

    public VectorStoreService(GeminiClient geminiClient, EmbeddingService embeddingService) {
        this.geminiClient = geminiClient;
        this.embeddingService = embeddingService;
    }

    public void addDocument(String content, String entityType, Long entityId, Map<String, Object> metadata) {
        try {
            List<Double> embedding = generateEmbedding(content);
            if (embedding == null || embedding.isEmpty()) {
                log.warn("Generated an empty or null embedding for document type {} with ID {}. Skipping.", entityType, entityId);
                return;
            }
            DocumentEmbedding doc = new DocumentEmbedding(
                    UUID.randomUUID().toString(),
                    content,
                    entityType,
                    entityId,
                    embedding,
                    metadata,
                    LocalDateTime.now()
            );
            embeddings.add(doc);
            log.info("Added document embedding for {} with ID {} (content preview: {})",
                    entityType, entityId, content.substring(0, Math.min(content.length(), 100)) + "...");
        } catch (Exception e) {
            log.error("Failed to add document embedding for {} with ID {}: {}", entityType, entityId, e.getMessage());
        }
    }

    /**
     * Searches for documents based on a query, with optional entity type and metadata filters.
     *
     * @param query The semantic search query string.
     * @param maxResults The maximum number of documents to return after filtering and similarity.
     * @param threshold The minimum cosine similarity to consider a document relevant.
     * @param targetEntityTypes An optional list of entity types to restrict the search to. If null or empty, all types are searched.
     * @param metadataFilters A map of metadata key-value pairs to filter documents.
     * Values can be single objects (for equality) or Collections (for 'in' operator).
     * @return A list of retrieved documents.
     */
    public List<RetrievedDocument> search(String query, int maxResults, double threshold,
                                          List<String> targetEntityTypes, Map<String, Object> metadataFilters) {
        try {
            log.info("Searching for query: '{}' with threshold: {} and maxResults: {}. Target types: {}. Filters: {}",
                    query, threshold, maxResults, targetEntityTypes, metadataFilters);
            log.info("Total documents in vector store: {}", embeddings.size());

            List<Double> queryEmbedding = generateEmbedding(query);
            if (queryEmbedding == null || queryEmbedding.isEmpty()) {
                log.warn("Failed to generate embedding for query: '{}'. Returning empty results.", query);
                return Collections.emptyList();
            }

            // Start with a stream of all embeddings
            java.util.stream.Stream<DocumentEmbedding> filteredStream = embeddings.stream();

            // 1. Apply entity type filter if specified
            if (targetEntityTypes != null && !targetEntityTypes.isEmpty()) {
                Set<String> typeSet = new HashSet<>(targetEntityTypes); // For faster lookup
                filteredStream = filteredStream.filter(doc -> typeSet.contains(doc.getEntityType()));
                log.debug("Applied entity type filter. Remaining documents (before metadata filter): {}", filteredStream.count());
                // Reset stream after count if needed for subsequent operations, or count before
                filteredStream = embeddings.stream().filter(doc -> typeSet.contains(doc.getEntityType()));
            }

            // 2. Apply metadata filters
            if (metadataFilters != null && !metadataFilters.isEmpty()) {
                for (Map.Entry<String, Object> entry : metadataFilters.entrySet()) {
                    String filterKey = entry.getKey();
                    Object filterValue = entry.getValue();

                    // Build a predicate for the current filter
                    Predicate<DocumentEmbedding> currentFilter = doc -> {
                        Object docValue = doc.getMetadata().get(filterKey);

                        // Handle null values for metadata
                        if (docValue == null) {
                            return filterValue == null; // Match if both are null
                        }
                        if (filterValue == null) {
                            return false; // No match if docValue is non-null but filterValue is null
                        }

                        // Handle 'IN' operator for collections (e.g., productionLineIds)
                        if (filterValue instanceof Collection) {
                            Collection<?> filterCollection = (Collection<?>) filterValue;
                            if (docValue instanceof Collection) {
                                // Both are collections, check for any intersection
                                return ((Collection<?>) docValue).stream().anyMatch(filterCollection::contains);
                            } else {
                                // Document value is single, filter value is collection, check if doc value is in filter collection
                                return filterCollection.contains(docValue);
                            }
                        }
                        // Handle simple equality for other types (e.g., boolean, single ID)
                        return docValue.equals(filterValue);
                    };
                    filteredStream = filteredStream.filter(currentFilter);
                }
            }
            log.debug("Applied metadata filters.");


            // 3. Calculate similarity, filter by threshold, sort, and limit
            List<RetrievedDocument> results = filteredStream
                    .map(doc -> {
                        double similarity = calculateCosineSimilarity(queryEmbedding, doc.getEmbedding());
                        return new RetrievedDocument(
                                doc.getContent(),
                                doc.getEntityType(),
                                doc.getEntityId(),
                                similarity,
                                doc.getMetadata()
                        );
                    })
                    .filter(doc -> doc.getSimilarity() >= threshold)
                    .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                    .limit(maxResults)
                    .collect(Collectors.toList());

            log.info("Found {} documents after all filters and above threshold {}", results.size(), threshold);
            if (!results.isEmpty()) {
                log.info("Top result similarity: {}, type: {}",
                        String.format("%.3f", results.get(0).getSimilarity()),
                        results.get(0).getEntityType());
            }

            return results;
        } catch (Exception e) {
            log.error("Search failed for query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Double> generateEmbedding(String text) {
        return embeddingService.generateEmbedding(text);
    }

    private double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty() || vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator > 0 ? dotProduct / denominator : 0.0;
    }

    // Add method to check vector store contents
    public void logVectorStoreStats() {
        log.info("=== Vector Store Statistics ===");
        log.info("Total documents: {}", embeddings.size());

        Map<String, Long> typeCounts = embeddings.stream()
                .collect(Collectors.groupingBy(DocumentEmbedding::getEntityType, Collectors.counting()));

        typeCounts.forEach((type, count) -> log.info("{}: {} documents", type, count));

        // Log some cycle_time examples if they exist
        embeddings.stream()
                .filter(doc -> "cycle_time".equals(doc.getEntityType()))
                .limit(3)
                .forEach(doc -> log.info("Sample cycle_time document: {}",
                        doc.getContent().substring(0, Math.min(doc.getContent().length(), 200)) + "..."));
    }
}
