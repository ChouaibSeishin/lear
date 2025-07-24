package org.lear.aibotservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "rag.indexing")
@Data
@Component
public class RAGIndexingProperties {
    private boolean autoStart = true;
    private int batchSize = 100;
}


