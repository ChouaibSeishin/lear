package com.lear.machine.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLineResponse {
    private Integer id;
    private String name;
    private String description;
    private List<MachineResponse> machines;
} 
