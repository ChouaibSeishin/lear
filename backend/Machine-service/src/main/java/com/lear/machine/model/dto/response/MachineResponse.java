package com.lear.machine.model.dto.response;

import com.lear.machine.model.enums.MachineType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponse {
    private Integer id;
    private String name;
    private String brand;
    private String description;
    private MachineType type;
    private LocalDateTime createdAt;
    private Integer productionLineId;
    private List<StepResponse> steps;
    private List<HistoryLogResponse> historyLogs;
} 