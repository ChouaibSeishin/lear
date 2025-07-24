package org.lear.importservice.dtos;

import lombok.Data;

@Data
public class StepRow {
    private String name;
    private String description;
    private Integer orderIndex;
    private Boolean requiresManualTracking;
    private String machineName;
}
