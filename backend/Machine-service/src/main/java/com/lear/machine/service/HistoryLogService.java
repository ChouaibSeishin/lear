package com.lear.machine.service;

import com.lear.machine.model.dto.request.HistoryLogRequest;
import com.lear.machine.model.dto.response.HistoryLogResponse;

import java.util.List;

public interface HistoryLogService {
    HistoryLogResponse createHistoryLog(HistoryLogRequest request);
    HistoryLogResponse getHistoryLogById(Integer id);
    List<HistoryLogResponse> getHistoryLogsByMachineId(Integer machineId);
} 