package com.lear.machine.service;

import com.lear.machine.model.dto.request.MachineRequest;
import com.lear.machine.model.dto.response.MachineResponse;

import java.util.List;
import java.util.Optional;

public interface MachineService {
    MachineResponse createMachine(MachineRequest request);
    MachineResponse getMachineById(Integer id);
    List<MachineResponse> getAllMachines();
    MachineResponse updateMachine(Integer id, MachineRequest request);
    void deleteMachine(Integer id);
    Optional<Integer> getMachineIdByName(String name);


} 
