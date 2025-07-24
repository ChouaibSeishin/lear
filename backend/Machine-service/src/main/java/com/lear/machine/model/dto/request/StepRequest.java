package com.lear.machine.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequest {
    private String name;
    private String description;
    private Integer orderIndex;
    private Boolean requiresManualTracking;
    private Integer machineId;
} 