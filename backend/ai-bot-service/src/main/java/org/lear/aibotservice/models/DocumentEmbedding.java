package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEmbedding {
    private String id;
    private String content;
    private String entityType; // "machine", "project", "cycle_time", etc.
    private Long entityId;
    private List<Double> embedding;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
