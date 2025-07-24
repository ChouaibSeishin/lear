package com.lear.machine.controller;

import com.lear.machine.model.dto.request.StepRequest;
import com.lear.machine.model.dto.response.StepResponse;
import com.lear.machine.service.StepService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/steps",produces = MediaType.APPLICATION_JSON_VALUE)
public class StepController {

    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StepResponse> createStep(@Valid @RequestBody StepRequest request) {
        StepResponse response = stepService.createStep(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<StepResponse> getStep(@PathVariable Integer id) {
        StepResponse response = stepService.getStepById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<StepResponse>> getStepsByMachine(@PathVariable Integer machineId) {
        List<StepResponse> responses = stepService.getStepsByMachineId(machineId);
        return ResponseEntity.ok(responses);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<StepResponse> updateStep(
            @PathVariable Integer id,
            @Valid @RequestBody StepRequest request) {
        StepResponse response = stepService.updateStep(id, request);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStep(@PathVariable Integer id) {
        stepService.deleteStep(id);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/name/{name}/id")
    public ResponseEntity<?> getStepIdByName(@PathVariable String name){

        return ResponseEntity.ok(stepService.getStepIdByName(name));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<?> getAllSteps(){
        return ResponseEntity.ok(stepService.getAllSteps());
    }
} 
