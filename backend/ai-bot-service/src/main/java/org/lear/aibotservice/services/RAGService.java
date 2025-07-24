package org.lear.aibotservice.services;

import lombok.extern.slf4j.Slf4j;
import org.lear.aibotservice.models.RAGQuery;
import org.lear.aibotservice.models.RAGResponse;
import org.lear.aibotservice.models.RetrievedDocument;
import org.lear.aibotservice.utilities.RAGQueryProcessor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RAGService {

    private final VectorStoreService vectorStore;
    private final EnhancedGeminiClient geminiClient;
    private final DataIndexingService indexingService;
    private final RAGQueryProcessor queryProcessor;

    public RAGService(VectorStoreService vectorStore, EnhancedGeminiClient geminiClient,
                      DataIndexingService indexingService, RAGQueryProcessor queryProcessor) {
        this.vectorStore = vectorStore;
        this.geminiClient = geminiClient;
        this.indexingService = indexingService;
        this.queryProcessor = queryProcessor;
    }

    // This is the primary method that should receive the preprocessed RAGQuery
    public RAGResponse ask(RAGQuery query) {
        try {
            log.info("Processing RAG query: '{}'. Target Entity Types: {}. Metadata Filters: {}",
                    query.getQuestion(), query.getEntityTypes(), query.getMetadataFilters());

            // Step 1: Retrieve relevant documents using the enhanced search method
            // Pass the extracted entity types and metadata filters
            List<RetrievedDocument> retrievedDocs = vectorStore.search(
                    query.getQuestion(),
                    query.getMaxResults(),
                    query.getSimilarityThreshold(),
                    query.getEntityTypes(),      // NEW: Pass targetEntityTypes
                    query.getMetadataFilters()   // NEW: Pass metadataFilters
            );

            if (retrievedDocs.isEmpty()) {
                return new RAGResponse(
                        "I couldn't find relevant information to answer your question. Please try rephrasing or ask about machines, production lines, steps, projects, variants, or cycle times.",
                        Collections.emptyList(),
                        0.0,
                        "No relevant documents found in the knowledge base."
                );
            }

            // Step 2: Generate response using retrieved context
            // Ensure generateRAGResponse can handle the new metadata in RetrievedDocument if it uses them
            String answer = geminiClient.generateRAGResponse(query.getQuestion(), retrievedDocs);

            // Step 3: Calculate confidence based on similarity scores
            double confidence;
            if (!retrievedDocs.isEmpty()) {
                // Calculate max similarity for confidence
                confidence = retrievedDocs.stream()
                        .mapToDouble(RetrievedDocument::getSimilarity)
                        .max() // Use max() instead of average()
                        .orElse(0.0);
            } else {
                confidence = 0.0;
            }

            // Enhance reasoning to include details about applied filters if any
            StringBuilder reasoningBuilder = new StringBuilder();
            reasoningBuilder.append(String.format(
                    "Found %d relevant documents with average similarity of %.2f. ",
                    retrievedDocs.size(), confidence
            ));

            if (query.getMetadataFilters() != null && !query.getMetadataFilters().isEmpty()) {
                reasoningBuilder.append("Filters applied: ").append(query.getMetadataFilters()).append(". ");
            }

            reasoningBuilder.append(String.format(
                    "Response generated from %s data.",
                    retrievedDocs.stream()
                            .map(RetrievedDocument::getEntityType)
                            .distinct()
                            .collect(Collectors.joining(", "))
            ));


            return new RAGResponse(answer, retrievedDocs, confidence, reasoningBuilder.toString());

        } catch (Exception e) {
            log.error("RAG query failed: {}", e.getMessage());
            return new RAGResponse(
                    "I encountered an error while processing your question. Please try again.",
                    Collections.emptyList(),
                    0.0,
                    "Error: " + e.getMessage()
            );
        }
    }

    // Convenience method for simple string queries
    // This method ensures all queries go through preprocessing
    public RAGResponse ask(String question) {
        // Always preprocess the raw question to extract entity types and metadata filters
        RAGQuery processedQuery = queryProcessor.preprocessQuery(question);
        return ask(processedQuery); // Call the main ask method with the fully prepared RAGQuery
    }

    public void reindexData() {
        log.info("Reindexing data...");
        indexingService.initializeIndex();
    }
}
