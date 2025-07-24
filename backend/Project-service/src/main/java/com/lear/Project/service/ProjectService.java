package com.lear.Project.service;

import com.lear.Project.dto.request.ProjectRequest;
import com.lear.Project.dto.response.ProjectResponse;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse getProjectById(Long id);
    List<ProjectResponse> getAllProjects();
    ProjectResponse updateProject(Long id, ProjectRequest request);
    Optional<Long> getProjectIdByName(String name);
    void deleteProject(Long id);
} 
