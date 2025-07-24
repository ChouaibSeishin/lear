package com.lear.machine.controller;

import com.lear.machine.model.dto.request.ProductionLineRequest;
import com.lear.machine.model.dto.response.ProductionLineResponse;
import com.lear.machine.service.ProductionLineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/production-lines")
public class ProductionLineController {

    private final ProductionLineService productionLineService;

    public ProductionLineController(ProductionLineService productionLineService) {
        this.productionLineService = productionLineService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductionLineResponse> createProductionLine(@Valid @RequestBody ProductionLineRequest request) {
        ProductionLineResponse response = productionLineService.createProductionLine(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductionLineResponse> getProductionLine(@PathVariable Integer id) {
        ProductionLineResponse response = productionLineService.getProductionLineById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductionLineResponse>> getAllProductionLines() {
        List<ProductionLineResponse> responses = productionLineService.getAllProductionLines();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductionLineResponse> updateProductionLine(
            @PathVariable Integer id,
            @Valid @RequestBody ProductionLineRequest request) {
        ProductionLineResponse response = productionLineService.updateProductionLine(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductionLine(@PathVariable Integer id) {
        productionLineService.deleteProductionLine(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping("/getByIds")
    public ResponseEntity<?> getByIds(@RequestParam List<Integer> ids) {
        List<ProductionLineResponse> productionLineResponseList = new ArrayList<>();
        ids.forEach(id -> productionLineResponseList.add(productionLineService.getProductionLineById(id)));
        return ResponseEntity.ok(productionLineResponseList);

    }

    @GetMapping("/name/{name}/id")
    public ResponseEntity<?>getLineIdByName(@PathVariable String name){
      return ResponseEntity.ok(productionLineService.getLineIdByName(name));
    }
} 
