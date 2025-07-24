package org.lear.userservice.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLogRequest {
    private Long userId;
    private String action;
    private LocalDateTime timestamp;
}
