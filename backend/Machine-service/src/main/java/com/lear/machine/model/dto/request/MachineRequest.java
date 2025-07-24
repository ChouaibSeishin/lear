package com.lear.machine.model.dto.request;

import com.lear.machine.model.enums.MachineType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MachineRequest {
    private String name;
    private String brand;
    private String description;
    private MachineType type;
    private Integer productionLineId;
} 