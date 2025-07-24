package com.lear.machine.service.impl;

import com.lear.machine.model.dto.request.ProductionLineRequest;
import com.lear.machine.model.dto.response.ProductionLineResponse;
import com.lear.machine.model.dto.response.MachineResponse;
import com.lear.machine.model.dto.response.StepResponse;
import com.lear.machine.model.entity.ProductionLineEntity;
import com.lear.machine.repository.ProductionLineRepository;
import com.lear.machine.service.MachineService;
import com.lear.machine.service.ProductionLineService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductionLineServiceImpl implements ProductionLineService {

    private final ProductionLineRepository productionLineRepository;
    private final MachineServiceImpl machineService;

    public ProductionLineServiceImpl(ProductionLineRepository productionLineRepository,MachineServiceImpl machineService) {
        this.productionLineRepository = productionLineRepository;
        this.machineService = machineService;
    }

    @Override
    @Transactional
    public ProductionLineResponse createProductionLine(ProductionLineRequest request) {
        ProductionLineEntity productionLine = new ProductionLineEntity();
        productionLine.setName(request.getName());
        productionLine.setDescription(request.getDescription());

        ProductionLineEntity savedProductionLine = productionLineRepository.save(productionLine);
        return mapToResponse(savedProductionLine);
    }

    @Override
    public ProductionLineResponse getProductionLineById(Integer id) {
        ProductionLineEntity productionLine = productionLineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Production line not found"));
        return mapToResponse(productionLine);
    }

    @Override
    public List<ProductionLineResponse> getAllProductionLines() {
        return productionLineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductionLineResponse updateProductionLine(Integer id, ProductionLineRequest request) {
        ProductionLineEntity productionLine = productionLineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Production line not found"));

        productionLine.setName(request.getName());
        productionLine.setDescription(request.getDescription());

        ProductionLineEntity updatedProductionLine = productionLineRepository.save(productionLine);
        return mapToResponse(updatedProductionLine);
    }

    @Override
    @Transactional
    public void deleteProductionLine(Integer id) {
        if (!productionLineRepository.existsById(id)) {
            throw new EntityNotFoundException("Production line not found");
        }
        productionLineRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> getLineIdByName(String name) {
        return  productionLineRepository.findByName(name).map(ProductionLineEntity::getId);

    }

    private ProductionLineResponse mapToResponse(ProductionLineEntity productionLine) {
        ProductionLineResponse response = new ProductionLineResponse();
        response.setId(productionLine.getId());
        response.setName(productionLine.getName());
        response.setDescription(productionLine.getDescription());

        if (productionLine.getMachines() != null && !productionLine.getMachines().isEmpty()) {
            response.setMachines(productionLine.getMachines().stream()
                    .map(machine -> {
                        MachineResponse machineResponse = new MachineResponse();
                        machineResponse.setId(machine.getId());
                        machineResponse.setName(machine.getName());
                        machineResponse.setBrand(machine.getBrand());
                        machineResponse.setDescription(machine.getDescription());
                        machineResponse.setType(machine.getType());
                        machineResponse.setCreatedAt(machine.getCreatedAt());


                        if (machine.getProductionLine() != null) {
                            machineResponse.setProductionLineId(machine.getProductionLine().getId());
                        } else {
                            machineResponse.setProductionLineId(null);
                        }

                        if (machine.getSteps() != null && !machine.getSteps().isEmpty()) {
                            machineResponse.setSteps(machine.getSteps().stream()
                                    .map(step -> {
                                        StepResponse stepResponse = new StepResponse();
                                        stepResponse.setId(step.getId());
                                        stepResponse.setName(step.getName());
                                        stepResponse.setDescription(step.getDescription());
                                        stepResponse.setOrderIndex(step.getOrderIndex());
                                        stepResponse.setRequiresManualTracking(step.getRequiresManualTracking());
                                        return stepResponse;
                                    }).collect(Collectors.toList())
                            );
                        } else {
                            machineResponse.setSteps(new ArrayList<>());
                        }

                        return machineResponse;
                    })
                    .collect(Collectors.toList()));
        } else {
            response.setMachines(new ArrayList<>());
        }

        return response;
    }
    public MachineService getMachineService() {
        return machineService;
    }
}
