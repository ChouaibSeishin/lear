package org.lear.importservice.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CycleTimeRow {
    private String projectName;
    private String variantName;
    private String lineName;
    private String stepName;
    private String machineName;
    private String userEmail;
    private String clientCycleTime;
    private String theoriticalCycleTime;
    private String startTime;
    private String endTime;
    private Boolean isManual;
    private String status;
    private String recordType;
}
