package org.lear.cycletimeservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserLogRequest {
    private Long userId;
    private String action;
    private LocalDateTime timestamp;
}
