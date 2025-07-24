package org.lear.cycletimeservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.lear.cycletimeservice.dtos.enums.RecordType;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Table(name = "cycle_time")
public class CycleTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long projectId;
    private Long variantId;
    private Long lineId;
    private Long machineId;
    private Long stepId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private Boolean isManual;
    private Duration clientCycleTime;
    private Duration theoriticalCycleTime;

    private String status;
    @Enumerated(EnumType.STRING)
    private RecordType recordType;

    public String getFormattedDuration() {
        if (duration == null || duration < 0) return "N/A";

        long millis = duration;
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        long remainingMillis = millis % 1000;

        if (minutes > 0) {
            return String.format("%d min %d.%03d sec", minutes, seconds, remainingMillis);
        } else {
            return String.format("%d.%03d sec", seconds, remainingMillis);
        }
    }

}
