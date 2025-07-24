package org.lear.cycletimeservice.controllers;

import lombok.RequiredArgsConstructor;
import org.lear.cycletimeservice.Services.CycleTimeService;
import org.lear.cycletimeservice.dtos.CycleTimeRequest;
import org.lear.cycletimeservice.entities.CycleTime;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/cycle-times", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CycleTimeController {

    private final CycleTimeService service;

    @PreAuthorize("hasAnyRole('AUDIT', 'ADMIN')")
    @PostMapping()
    public ResponseEntity<CycleTime> create(@RequestBody CycleTimeRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PreAuthorize("hasAnyRole('AUDIT', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CycleTime> update(@PathVariable Long id, @RequestBody CycleTimeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAnyRole('AUDIT', 'ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping
    public List<CycleTime> getAll() {
        return service.getAll();
    }

    @PreAuthorize("hasAnyRole('USER', 'AUDIT', 'ADMIN')")
    @GetMapping("/{id}")
    public CycleTime getById(@PathVariable Long id) {
        return service.getById(id);
    }

}
