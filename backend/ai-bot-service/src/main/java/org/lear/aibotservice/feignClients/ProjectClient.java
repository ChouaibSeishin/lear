package org.lear.aibotservice.feignClients;

import org.lear.aibotservice.models.Project;
import org.lear.aibotservice.models.Variant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "project-service",url = "gateway-service:8008/project-service")

public interface ProjectClient {
    @GetMapping("/api/projects")
    List<Project> getAllProjects();

    @GetMapping("/api/variants")
    List<Variant> getAllVariants();


    @GetMapping("/api/projects/{id}")
    Project getProject(@PathVariable("id") Long id);

    @GetMapping("/api/variants/{id}")
    Variant getVariant(@PathVariable("id") Long id);
}
