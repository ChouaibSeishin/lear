package org.lear.aibotservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "rag.vector-store")
@Data
@Component
public class RAGVectorStoreProperties {
    private double similarityThreshold = 0.3;
    private int maxResults = 50;
}
