package com.lear.machine.model.dto.response;

import com.lear.machine.model.entity.MachineEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer orderIndex;
    private Boolean requiresManualTracking;
    private Integer machineId;
} 
