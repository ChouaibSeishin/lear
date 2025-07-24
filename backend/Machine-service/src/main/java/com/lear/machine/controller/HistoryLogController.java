package com.lear.machine.controller;

import com.lear.machine.model.dto.request.HistoryLogRequest;
import com.lear.machine.model.dto.response.HistoryLogResponse;
import com.lear.machine.service.HistoryLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/history-logs",produces = MediaType.APPLICATION_JSON_VALUE)
public class HistoryLogController {

    private final HistoryLogService historyLogService;

    public HistoryLogController(HistoryLogService historyLogService) {
        this.historyLogService = historyLogService;
    }


    @PostMapping
    public ResponseEntity<HistoryLogResponse> createHistoryLog(@Valid @RequestBody HistoryLogRequest request) {
        HistoryLogResponse response = historyLogService.createHistoryLog(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryLogResponse> getHistoryLog(@PathVariable Integer id) {
        HistoryLogResponse response = historyLogService.getHistoryLogById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<HistoryLogResponse>> getHistoryLogsByMachine(@PathVariable Integer machineId) {
        List<HistoryLogResponse> responses = historyLogService.getHistoryLogsByMachineId(machineId);
        return ResponseEntity.ok(responses);
    }
} 
