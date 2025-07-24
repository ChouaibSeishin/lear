package com.lear.Project.service;

import com.lear.Project.dto.request.VariantRequest;
import com.lear.Project.dto.response.VariantResponse;

import java.util.List;
import java.util.Optional;

public interface VariantService {
    VariantResponse createVariant(VariantRequest request);
    VariantResponse getVariantById(Long id);
    List<VariantResponse> getAllVariants();
    List<VariantResponse> getVariantsByProjectId(Long projectId);
    Optional<Long> getVariantIdByName(String name);
    VariantResponse updateVariant(Long id, VariantRequest request);
    void deleteVariant(Long id);
} 
