package com.lear.Project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequest {
    private String name;
    private String status;
    private Long projectId;
    private Set<Long> productionLineIds = new HashSet<>();

} 
