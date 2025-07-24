package com.lear.machine.controller;

import com.lear.machine.model.dto.request.MachineRequest;
import com.lear.machine.model.dto.response.MachineResponse;
import com.lear.machine.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/machines",produces = MediaType.APPLICATION_JSON_VALUE)
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MachineResponse> createMachine(@Valid @RequestBody MachineRequest request) {
        MachineResponse response = machineService.createMachine(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<MachineResponse> getMachine(@PathVariable Integer id) {
        MachineResponse response = machineService.getMachineById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<MachineResponse>> getAllMachines() {
        List<MachineResponse> responses = machineService.getAllMachines();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MachineResponse> updateMachine(
            @PathVariable Integer id,
            @Valid @RequestBody MachineRequest request) {
        MachineResponse response = machineService.updateMachine(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Integer id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/name/{name}/id")
    public ResponseEntity<?> getMachineIdByName(@PathVariable String name){

        return ResponseEntity.ok(machineService.getMachineIdByName(name));
    }

}
