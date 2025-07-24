package org.lear.aibotservice.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievedDocument {
    private String content;
    private String entityType;
    private Long entityId;
    private Double similarity;
    private Map<String, Object> metadata;
}
