package com.lear.Project.service.impl;

import com.lear.Project.dto.request.VariantRequest;
import com.lear.Project.dto.response.VariantResponse;
import com.lear.Project.entity.Project;
import com.lear.Project.entity.Variant;
import com.lear.Project.repository.ProjectRepository;
import com.lear.Project.repository.VariantRepository;
import com.lear.Project.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ProjectRepository projectRepository;

    @Override
    public VariantResponse createVariant(VariantRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Variant variant = new Variant();
        variant.setName(request.getName());
        variant.setStatus(request.getStatus());
        variant.setProductionLineIds(request.getProductionLineIds());
        variant.setProject(project);

        Variant savedVariant = variantRepository.save(variant);
        return mapToResponse(savedVariant);
    }

    @Override
    public VariantResponse getVariantById(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        return mapToResponse(variant);
    }

    @Override
    public List<VariantResponse> getAllVariants() {
        return variantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VariantResponse> getVariantsByProjectId(Long projectId) {
        return variantRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Long> getVariantIdByName(String name) {

        return variantRepository.findByName(name).map(Variant::getId);
    }

    @Override
    public VariantResponse updateVariant(Long id, VariantRequest request) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        variant.setName(request.getName());
        variant.setStatus(request.getStatus());
        variant.setProductionLineIds(request.getProductionLineIds());
        variant.setProject(project);

        Variant updatedVariant = variantRepository.save(variant);
        return mapToResponse(updatedVariant);
    }

    @Override
    public void deleteVariant(Long id) {
        variantRepository.deleteById(id);
    }

    private VariantResponse mapToResponse(Variant variant) {
        VariantResponse response = new VariantResponse();
        response.setId(variant.getId());
        response.setName(variant.getName());
        response.setStatus(variant.getStatus());
        response.setProductionLineIds(variant.getProductionLineIds());
        response.setProjectId(variant.getProject().getId());
        return response;
    }
} 
