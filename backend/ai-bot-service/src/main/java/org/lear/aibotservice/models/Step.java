package org.lear.aibotservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step {
    private Long id;
    private String name;
    private Integer orderIndex;
    private boolean requiresManualTracking;
    private Integer machineId;

  }
