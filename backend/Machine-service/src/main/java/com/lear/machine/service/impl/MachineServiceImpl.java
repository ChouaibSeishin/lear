package com.lear.machine.service.impl;

import com.lear.machine.model.dto.request.MachineRequest;
import com.lear.machine.model.dto.response.MachineResponse;
import com.lear.machine.model.dto.response.StepResponse;
import com.lear.machine.model.entity.MachineEntity;
import com.lear.machine.model.entity.ProductionLineEntity;

import com.lear.machine.repository.MachineRepository;
import com.lear.machine.repository.ProductionLineRepository;
import com.lear.machine.service.MachineService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;
    private final ProductionLineRepository productionLineRepository;

    public MachineServiceImpl(MachineRepository machineRepository,
                            ProductionLineRepository productionLineRepository) {
        this.machineRepository = machineRepository;
        this.productionLineRepository = productionLineRepository;
    }

    @Override
    @Transactional
    public MachineResponse createMachine(MachineRequest request) {
        ProductionLineEntity productionLine = null;
        if (request.getProductionLineId() != null) {
            productionLine = productionLineRepository.findById(request.getProductionLineId())
                    .orElseThrow(() -> new EntityNotFoundException("Production line not found"));
        }

        MachineEntity machine = new MachineEntity();
        machine.setName(request.getName());
        machine.setBrand(request.getBrand());
        machine.setDescription(request.getDescription());
        machine.setType(request.getType());
        machine.setProductionLine(productionLine);
        machine.setCreatedAt(LocalDateTime.now());

        MachineEntity savedMachine = machineRepository.save(machine);
        return mapToResponse(savedMachine);
    }

    @Override
    public MachineResponse getMachineById(Integer id) {
        MachineEntity machine = machineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found"));
        return mapToResponse(machine);
    }

    @Override
    public List<MachineResponse> getAllMachines() {
        return machineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MachineResponse updateMachine(Integer id, MachineRequest request) {
        MachineEntity machine = machineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found"));

        ProductionLineEntity productionLine = null;
        if (request.getProductionLineId() != null) {
            productionLine = productionLineRepository.findById(request.getProductionLineId())
                    .orElseThrow(() -> new EntityNotFoundException("Production line not found"));
        }

        machine.setName(request.getName());
        machine.setBrand(request.getBrand());
        machine.setDescription(request.getDescription());
        machine.setType(request.getType());
        machine.setProductionLine(productionLine);

        MachineEntity updatedMachine = machineRepository.save(machine);
        return mapToResponse(updatedMachine);
    }

    @Override
    @Transactional
    public void deleteMachine(Integer id) {
        if (!machineRepository.existsById(id)) {
            throw new EntityNotFoundException("Machine not found");
        }
        machineRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> getMachineIdByName(String name) {
        return  machineRepository.findByName(name).map(MachineEntity::getId);
    }

    private MachineResponse mapToResponse(MachineEntity machine) {
        MachineResponse response = new MachineResponse();
        response.setId(machine.getId());
        response.setName(machine.getName());
        response.setBrand(machine.getBrand());
        response.setDescription(machine.getDescription());
        response.setType(machine.getType());
        response.setCreatedAt(machine.getCreatedAt());


        if (machine.getProductionLine() != null) {
            response.setProductionLineId(machine.getProductionLine().getId());
        } else {
            response.setProductionLineId(null);
        }


        if (machine.getSteps() != null && !machine.getSteps().isEmpty()) {
            response.setSteps(machine.getSteps().stream()
                    .map(step -> {
                        StepResponse stepResponse = new StepResponse();
                        stepResponse.setId(step.getId());
                        stepResponse.setName(step.getName());
                        stepResponse.setDescription(step.getDescription());
                        stepResponse.setOrderIndex(step.getOrderIndex());
                        stepResponse.setRequiresManualTracking(step.getRequiresManualTracking());
                        return stepResponse;
                    })
                    .collect(Collectors.toList()));
        } else {
            response.setSteps(new ArrayList<>());
        }

        return response;
    }


} 
