package com.lear.machine.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryLogResponse {
    private Integer id;
    private Integer userId;
    private String action;
    private LocalDateTime timestamp;
} 