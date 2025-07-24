package org.lear.importservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "machine-service",url = "gateway-service:8008/machine-service")
public interface MachineServiceClient {
    @PostMapping("/api/production-lines")
    void createLine(@RequestBody Map<String, Object> body);

    @GetMapping("/api/production-lines/name/{name}/id")
        Long getLineIdByName(@PathVariable String name);

    @PutMapping("/api/production-lines/{id}")
    void updateLine(@PathVariable Long id, @RequestBody Map<String, Object> body);

    @PostMapping("/api/machines")
    void createMachine(@RequestBody Map<String, Object> body);

    @GetMapping("/api/machines/name/{name}/id")
    Long getMachineIdByName(@PathVariable String name);

    @PutMapping("/api/machines/{id}")
    void updateMachine(@PathVariable Long id, @RequestBody Map<String, Object> body);

    @PostMapping("/api/steps")
    void createStep(@RequestBody Map<String, Object> body);

    @GetMapping("/api/steps/name/{name}/id")
    Long getStepIdByName(@PathVariable String name);

    @PutMapping("/api/steps/{id}")
    void updateStep(@PathVariable Long id, @RequestBody Map<String, Object> body);
}
