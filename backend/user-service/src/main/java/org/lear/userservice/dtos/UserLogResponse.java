package org.lear.userservice.dtos;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserLogResponse {

    private Long id;
    private String action;
    private LocalDateTime timestamp;
    private Boolean seen;
    private UserDto userDto;
}
