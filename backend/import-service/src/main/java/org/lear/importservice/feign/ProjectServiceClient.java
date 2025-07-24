package org.lear.importservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "project-service",url = "gateway-service:8008/project-service")
public interface ProjectServiceClient {
    @PostMapping("/api/projects")
    void createProject(@RequestBody Map<String, Object> body);

    @GetMapping("/api/projects/name/{name}/id")
    Long getProjectIdByName(@PathVariable String name);

    @PutMapping("/api/projects/{id}")
    void updateProject(@PathVariable Long id, @RequestBody Map<String, Object> body);


    @PostMapping("/api/variants")
    void createVariant(@RequestBody Map<String, Object> body);

    @GetMapping("/api/variants/name/{name}/id")
    Long getVariantIdByName(@PathVariable String name);

    @PutMapping("/api/variants/{id}")
    void updateVariant(@PathVariable Long id, @RequestBody Map<String, Object> body);

}
