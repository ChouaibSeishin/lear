package org.lear.cycletimeservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.lear.cycletimeservice.dtos.enums.RecordType;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class CycleTimeRequest {
    private Long projectId;
    private Long variantId;
    private Long lineId;
    private Long stepId;
    private Long machineId;
    private Long userId;
    private Duration clientCycleTime;
    private Duration theoriticalCycleTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isManual;
    private String status;
    @JsonProperty
    private RecordType recordType;
}
