package org.lear.importservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "cycletime-service",url = "gateway-service:8008/cycletime-service")
public interface CycleTimeServiceClient {
    @PostMapping("/api/cycle-times")
    void createCycleTime(@RequestBody Map<String, Object> body);

    @PutMapping("/api/cycle-times/{id}")
    void updateCycleTime(@PathVariable Long id, @RequestBody Map<String, Object> body);

}
