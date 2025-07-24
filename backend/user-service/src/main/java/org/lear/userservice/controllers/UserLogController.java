package org.lear.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.lear.userservice.dtos.UserLogRequest;
import org.lear.userservice.dtos.UserLogResponse;
import org.lear.userservice.entities.UserLog;
import org.lear.userservice.services.UserLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class UserLogController {

    private final UserLogService userLogService;

    @PreAuthorize("hasAnyRole('AUDIT', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Void> logAction(@RequestBody UserLogRequest logRequest) {
        userLogService.logUserAction(
                logRequest.getUserId(),
                logRequest.getAction(),
                logRequest.getTimestamp()
        );
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('AUDIT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserLogResponse>> getUserLogs(@RequestParam Long userId) {
        return ResponseEntity.ok(userLogService.getUserLogs(userId));
    }

    @GetMapping("/")
    public ResponseEntity<List<UserLogResponse>> getAllLogs() {
        return ResponseEntity.ok(userLogService.getAllLogs());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserLogResponse> updateSeen(@PathVariable Long id, @RequestParam Boolean seen) {
        UserLog log = userLogService.getLog(id);
        log.setSeen(seen);
        return ResponseEntity.ok(userLogService.toUserLogResponse(log));
    }
}
