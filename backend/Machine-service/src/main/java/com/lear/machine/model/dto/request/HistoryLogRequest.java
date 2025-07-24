package com.lear.machine.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryLogRequest {
    private Integer machineId;
    private Integer userId;
    private String action;
} 