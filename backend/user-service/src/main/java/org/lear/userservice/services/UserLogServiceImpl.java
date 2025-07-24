package org.lear.userservice.services;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.lear.userservice.dtos.UserLogResponse;
import org.lear.userservice.entities.User;
import org.lear.userservice.entities.UserLog;
import org.lear.userservice.mapper.UserMapper;
import org.lear.userservice.repositories.UserLogRepository;
import org.lear.userservice.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class UserLogServiceImpl implements UserLogService {

    private final UserLogRepository userLogRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void logUserAction(Long userId, String action, LocalDateTime timestamp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserLog log = UserLog.builder()
                .user(user)
                .action(action)
                .timestamp(timestamp)
                .seen(false)
                .build();

        userLogRepository.save(log);
    }

    @Override
    public List<UserLogResponse> getUserLogs(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userLogRepository.findByUser(user).stream()
                .map(this::toUserLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserLogResponse> getAllLogs() {
        return userLogRepository.findAll().stream()
                .map(this::toUserLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserLog getLog(Long logId) {
        return userLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Log not found"));
    }

    @Override
    public UserLogResponse toUserLogResponse(UserLog log) {
        UserLogResponse response = new UserLogResponse();
        response.setId(log.getId());
        response.setAction(log.getAction());
        response.setTimestamp(log.getTimestamp());
        response.setSeen(log.getSeen());
        response.setUserDto(userMapper.userToUserDto(log.getUser()));
        return response;
    }
}
