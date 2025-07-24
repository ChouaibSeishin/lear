package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RAGQuery {
    private String question;
    private String context;
    private List<String> entityTypes;
    private Integer maxResults ;
    private Double similarityThreshold;
    private Map<String, Object> metadataFilters;
}
