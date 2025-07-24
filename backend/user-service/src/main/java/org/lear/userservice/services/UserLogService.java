package org.lear.userservice.services;

import org.lear.userservice.dtos.UserLogResponse;
import org.lear.userservice.entities.User;
import org.lear.userservice.entities.UserLog;

import java.time.LocalDateTime;
import java.util.List;

public interface UserLogService {
    void logUserAction(Long userId, String action, LocalDateTime timestamp);
    List<UserLogResponse> getUserLogs(Long userId);
    List<UserLogResponse> getAllLogs();
    UserLog getLog(Long logId);
    UserLogResponse toUserLogResponse(UserLog log);

}
