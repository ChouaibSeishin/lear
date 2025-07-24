package com.lear.Project.service.impl;

import com.lear.Project.dto.request.ProjectRequest;
import com.lear.Project.dto.response.ProjectResponse;
import com.lear.Project.dto.response.VariantResponse;
import com.lear.Project.entity.Project;
import com.lear.Project.repository.ProjectRepository;
import com.lear.Project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setProductionLineIds(request.getProductionLineIds());
        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setName(request.getName());
        project.setProductionLineIds(request.getProductionLineIds());
        project.setDescription(request.getDescription());
        Project updatedProject = projectRepository.save(project);
        return mapToResponse(updatedProject);
    }

    @Override
    public Optional<Long> getProjectIdByName(String name) {
        return projectRepository.findByName(name)
                .map(Project::getId);
    }



    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
    response.setProductionLineIds(project.getProductionLineIds());
        
        List<VariantResponse> variantResponses = project.getVariants().stream()
                .map(variant -> {
                    VariantResponse variantResponse = new VariantResponse();
                    variantResponse.setId(variant.getId());
                    variantResponse.setName(variant.getName());
                    variantResponse.setStatus(variant.getStatus());
                    variantResponse.setProjectId(variant.getProject().getId());
                    variantResponse.setProductionLineIds(variant.getProductionLineIds());
                    return variantResponse;
                })
                .collect(Collectors.toList());
        
        response.setVariants(variantResponses);
        return response;
    }
} 
