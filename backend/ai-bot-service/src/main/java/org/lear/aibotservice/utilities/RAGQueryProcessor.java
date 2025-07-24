package org.lear.aibotservice.utilities;

import lombok.extern.slf4j.Slf4j;
import org.lear.aibotservice.models.RAGQuery;
import org.lear.aibotservice.services.DataIndexingService; // Import DataIndexingService
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap; // Import HashMap
import java.util.HashSet;
import java.util.List; // Import List
import java.util.Map; // Import Map
import java.util.Set;

@Service
@Slf4j
public class RAGQueryProcessor {

    // Inject DataIndexingService to access lookup maps (e.g., productionLineNames)
    private final DataIndexingService dataIndexingService;

    public RAGQueryProcessor(DataIndexingService dataIndexingService) {
        this.dataIndexingService = dataIndexingService;
    }

    public RAGQuery preprocessQuery(String question) {
        RAGQuery query = new RAGQuery();
        query.setQuestion(question.trim());

        Set<String> detectedEntityTypes = new HashSet<>();
        Map<String, Object> metadataFilters = new HashMap<>(); // Initialize metadata filters
        String lowerQuestion = question.toLowerCase();

        // --- Intent-based Entity Type and Metadata Filter Detection ---

        // Prioritize comprehensive documents for complex relational queries
        if (lowerQuestion.contains("project") || lowerQuestion.contains("variant") || lowerQuestion.contains("deliverable")) {
            detectedEntityTypes.add("project_comprehensive");
        }
        if (lowerQuestion.contains("machine") || lowerQuestion.contains("equipment") || lowerQuestion.contains("step")) {
            detectedEntityTypes.add("machine_comprehensive");
        }

        // Detect specific filters and add them to metadataFilters
        // 1. "projects with no cycle times"
        if (lowerQuestion.contains("project") && lowerQuestion.contains("no cycle times")) {
            metadataFilters.put("hasCycleTime", false);
            detectedEntityTypes.add("project_comprehensive"); // Ensure we search comprehensive projects
            log.debug("Detected 'no cycle times' filter for projects.");
        }

        // 2. "what projects use prod-001" or "projects using prod-001"
        if (lowerQuestion.contains("project") && lowerQuestion.contains("use") && lowerQuestion.contains("prod-")) {
            // Extract the production line name, e.g., "prod-001"
            // This is a simplified regex; in a real app, use more robust NLP or another LLM call.
            String productionLineName = extractProductionLineName(lowerQuestion);
            if (productionLineName != null) {
                Long productionLineId = dataIndexingService.getProductionLineIdByName(productionLineName); // NEW: Requires method in DataIndexingService
                if (productionLineId != null) {
                    // For 'productionLineIds' metadata which is a List<Long>, use a List as the filter value.
                    // The VectorStoreService's 'in' logic will check if the document's list contains this ID.
                    metadataFilters.put("productionLineIds", List.of(productionLineId));
                    detectedEntityTypes.add("project_comprehensive");
                    log.debug("Detected filter for production line '{}' (ID: {}) on projects.", productionLineName, productionLineId);
                } else {
                    log.warn("Could not find ID for production line name: {}", productionLineName);
                }
            }
        }
        // Add similar logic for other filterable properties (e.g., machine status, step requirements)


        // Fallback for entity types if no specific comprehensive ones detected
        if (detectedEntityTypes.isEmpty()) {
            log.debug("No specific comprehensive entity types detected for query '{}', defaulting to all comprehensive.", question);
            detectedEntityTypes.add("project_comprehensive");
            detectedEntityTypes.add("machine_comprehensive");
            // Optionally, add other common granular types too if you want broader initial search
            // detectedEntityTypes.add("cycle_time");
            // detectedEntityTypes.add("production_line");
        }

        // Always add granular types too, if the query implies them, for broader semantic search capabilities
        // (even if a comprehensive document is also found)
        if (lowerQuestion.contains("machine") || lowerQuestion.contains("equipment")) {
            detectedEntityTypes.add("machine");
        }
        if (lowerQuestion.contains("step") || lowerQuestion.contains("process")) {
            detectedEntityTypes.add("step");
        }
        if (lowerQuestion.contains("project")) {
            detectedEntityTypes.add("project");
        }
        if (lowerQuestion.contains("variant")) {
            detectedEntityTypes.add("variant");
        }
        if (lowerQuestion.contains("cycle time") || lowerQuestion.contains("duration") || lowerQuestion.contains("throughput")) {
            detectedEntityTypes.add("cycle_time");
        }
        if (lowerQuestion.contains("production line") || lowerQuestion.contains("line")) {
            detectedEntityTypes.add("production_line");
        }


        query.setEntityTypes(new ArrayList<>(detectedEntityTypes));
        query.setMaxResults(50); // Adjust maxResults as needed, especially with filters
        query.setSimilarityThreshold(0.1);
        query.setMetadataFilters(metadataFilters); // Set the detected filters

        log.debug("Preprocessed query '{}'. Detected entity types: {}. Metadata Filters: {}",
                question, query.getEntityTypes(), query.getMetadataFilters());

        return query;
    }

    // Simple helper to extract production line name from query
    private String extractProductionLineName(String lowerQuestion) {
        // This is a very basic example. Consider using regex, or an LLM call for robust extraction.
        if (lowerQuestion.contains("prod-")) {
            int startIndex = lowerQuestion.indexOf("prod-");
            int endIndex = startIndex + "prod-".length();
            while (endIndex < lowerQuestion.length() && (Character.isDigit(lowerQuestion.charAt(endIndex)) || Character.isLetter(lowerQuestion.charAt(endIndex)))) {
                endIndex++;
            }
            return lowerQuestion.substring(startIndex, endIndex);
        }
        return null;
    }
}
