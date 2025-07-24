package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleTime {
    private Long id;
    private Long projectId;
    private Long variantId;
    private Long machineId;
    private Long lineId;
    private Long stepId;
    private String formattedDuration;
    private LocalDate startTime;
    private LocalDate endTime;
    private Long userId;
    private String status;

    }
