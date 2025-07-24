package com.lear.Project.controller;

import com.lear.Project.dto.request.VariantRequest;
import com.lear.Project.dto.response.VariantResponse;
import com.lear.Project.entity.Variant;
import com.lear.Project.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/variants",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<VariantResponse> createVariant(@RequestBody VariantRequest request) {
        return ResponseEntity.ok(variantService.createVariant(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDIT','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<VariantResponse> getVariant(@PathVariable Long id) {
        return ResponseEntity.ok(variantService.getVariantById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDIT','USER')")
    @GetMapping
    public ResponseEntity<List<VariantResponse>> getAllVariants() {
        return ResponseEntity.ok(variantService.getAllVariants());
    }
    @PreAuthorize("hasAnyRole('ADMIN','AUDIT','USER')")

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<VariantResponse>> getVariantsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(variantService.getVariantsByProjectId(projectId));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<VariantResponse> updateVariant(
            @PathVariable Long id,
            @RequestBody VariantRequest request) {
        return ResponseEntity.ok(variantService.updateVariant(id, request));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/name/{name}/id")
    public ResponseEntity<?> getVariantIdByName(@PathVariable String name){
        return ResponseEntity.ok(variantService.getVariantIdByName(name));
    }
} 
