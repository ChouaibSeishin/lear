package com.lear.machine.service;

import com.lear.machine.model.dto.request.StepRequest;
import com.lear.machine.model.dto.response.StepResponse;

import java.util.List;
import java.util.Optional;

public interface StepService {
    StepResponse createStep(StepRequest request);
    StepResponse getStepById(Integer id);
    List<StepResponse> getStepsByMachineId(Integer machineId);
    List<StepResponse> getAllSteps();
    StepResponse updateStep(Integer id, StepRequest request);
    void deleteStep(Integer id);
    Optional<Integer> getStepIdByName(String name);
} 
