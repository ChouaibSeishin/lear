package org.lear.aibotservice.services;

import org.lear.aibotservice.models.RetrievedDocument;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnhancedGeminiClient extends GeminiClient {

    public String generateRAGResponse(String question, List<RetrievedDocument> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant helping with production line management. ");
        prompt.append("Answer the following question based on the provided context. ");
        prompt.append("Be specific and cite relevant information from the context.\n\n");

        prompt.append("Question: ").append(question).append("\n\n");

        prompt.append("Context:\n");
        for (int i = 0; i < context.size(); i++) {
            RetrievedDocument doc = context.get(i);
            prompt.append("Document ").append(i + 1).append(" (").append(doc.getEntityType()).append("):\n");
            prompt.append(doc.getContent()).append("\n");
            prompt.append("Similarity Score: ").append(String.format("%.2f", doc.getSimilarity())).append("\n\n");
        }

        prompt.append("Please provide a comprehensive answer based on the context above. ");
        prompt.append("If the context doesn't contain enough information, mention what additional data would be helpful.");

        return ask(prompt.toString());
    }

    public String generateContextualQuery(String originalQuery, String entityType) {
        String prompt = String.format(
                "Expand the following query to be more specific for searching %s data: '%s'. " +
                        "Add relevant technical terms and context that would help find the most relevant information.",
                entityType, originalQuery
        );
        return ask(prompt);
    }
}
