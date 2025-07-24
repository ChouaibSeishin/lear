package com.lear.machine.service;

import com.lear.machine.model.dto.request.ProductionLineRequest;
import com.lear.machine.model.dto.response.ProductionLineResponse;

import java.util.List;
import java.util.Optional;

public interface ProductionLineService {
    ProductionLineResponse createProductionLine(ProductionLineRequest request);
    ProductionLineResponse getProductionLineById(Integer id);
    List<ProductionLineResponse> getAllProductionLines();
    ProductionLineResponse updateProductionLine(Integer id, ProductionLineRequest request);
    void deleteProductionLine(Integer id);
    Optional<Integer> getLineIdByName(String name);
} 
