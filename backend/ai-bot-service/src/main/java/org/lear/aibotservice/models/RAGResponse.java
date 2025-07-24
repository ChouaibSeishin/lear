package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RAGResponse {
    private String answer;
    private List<RetrievedDocument> sources;
    private Double confidence;
    private String reasoning;
}
