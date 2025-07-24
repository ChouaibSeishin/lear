package com.lear.machine.service.impl;

import com.lear.machine.model.dto.request.StepRequest;
import com.lear.machine.model.dto.response.StepResponse;
import com.lear.machine.model.entity.MachineEntity;
import com.lear.machine.model.entity.StepEntity;
import com.lear.machine.repository.MachineRepository;
import com.lear.machine.repository.StepRepository;
import com.lear.machine.service.StepService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StepServiceImpl implements StepService {

    private final StepRepository stepRepository;
    private final MachineRepository machineRepository;

    public StepServiceImpl(StepRepository stepRepository, MachineRepository machineRepository) {
        this.stepRepository = stepRepository;
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public StepResponse createStep(StepRequest request) {
        MachineEntity machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new EntityNotFoundException("Machine not found"));

        StepEntity step = new StepEntity();
        step.setMachine(machine);
        step.setName(request.getName());
        step.setDescription(request.getDescription());
        step.setOrderIndex(request.getOrderIndex());
        step.setRequiresManualTracking(request.getRequiresManualTracking());

        StepEntity savedStep = stepRepository.save(step);
        return mapToResponse(savedStep);
    }

    @Override
    public StepResponse getStepById(Integer id) {
        StepEntity step = stepRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Step not found"));
        return mapToResponse(step);
    }

    @Override
    public List<StepResponse> getStepsByMachineId(Integer machineId) {
        return stepRepository.findByMachineId(machineId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StepResponse> getAllSteps() {
        return stepRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StepResponse updateStep(Integer id, StepRequest request) {
        StepEntity step = stepRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Step not found"));

        MachineEntity machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new EntityNotFoundException("Machine not found"));

        step.setMachine(machine);
        step.setName(request.getName());
        step.setDescription(request.getDescription());
        step.setOrderIndex(request.getOrderIndex());
        step.setRequiresManualTracking(request.getRequiresManualTracking());

        StepEntity updatedStep = stepRepository.save(step);
        return mapToResponse(updatedStep);
    }

    @Override
    @Transactional
    public void deleteStep(Integer id) {
        if (!stepRepository.existsById(id)) {
            throw new EntityNotFoundException("Step not found");
        }
        stepRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> getStepIdByName(String name) {
        return stepRepository.findByName(name).map(StepEntity::getId);
    }

    private StepResponse mapToResponse(StepEntity step) {
        StepResponse response = new StepResponse();
        response.setId(step.getId());
        response.setName(step.getName());
        response.setDescription(step.getDescription());
        response.setOrderIndex(step.getOrderIndex());
        response.setRequiresManualTracking(step.getRequiresManualTracking());
        response.setMachineId(step.getMachine().getId());
        return response;
    }
} 
