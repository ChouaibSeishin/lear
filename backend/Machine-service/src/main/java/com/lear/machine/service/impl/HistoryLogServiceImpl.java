package com.lear.machine.service.impl;

import com.lear.machine.model.dto.request.HistoryLogRequest;
import com.lear.machine.model.dto.response.HistoryLogResponse;
import com.lear.machine.model.entity.HistoryLogEntity;
import com.lear.machine.model.entity.MachineEntity;
import com.lear.machine.repository.HistoryLogRepository;
import com.lear.machine.repository.MachineRepository;
import com.lear.machine.service.HistoryLogService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryLogServiceImpl implements HistoryLogService {

    private final HistoryLogRepository historyLogRepository;
    private final MachineRepository machineRepository;

    public HistoryLogServiceImpl(HistoryLogRepository historyLogRepository, MachineRepository machineRepository) {
        this.historyLogRepository = historyLogRepository;
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public HistoryLogResponse createHistoryLog(HistoryLogRequest request) {
        MachineEntity machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new EntityNotFoundException("Machine not found"));

        HistoryLogEntity historyLog = new HistoryLogEntity();
        historyLog.setMachine(machine);
        historyLog.setUserId(request.getUserId());
        historyLog.setAction(request.getAction());
        historyLog.setTimestamp(LocalDateTime.now());

        HistoryLogEntity savedHistoryLog = historyLogRepository.save(historyLog);
        return mapToResponse(savedHistoryLog);
    }

    @Override
    public HistoryLogResponse getHistoryLogById(Integer id) {
        HistoryLogEntity historyLog = historyLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("History log not found"));
        return mapToResponse(historyLog);
    }

    @Override
    public List<HistoryLogResponse> getHistoryLogsByMachineId(Integer machineId) {
        return historyLogRepository.findByMachineId(machineId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HistoryLogResponse mapToResponse(HistoryLogEntity historyLog) {
        HistoryLogResponse response = new HistoryLogResponse();
        response.setId(historyLog.getId());
        response.setUserId(historyLog.getUserId());
        response.setAction(historyLog.getAction());
        response.setTimestamp(historyLog.getTimestamp());
        return response;
    }
} 