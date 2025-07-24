package org.lear.aibotservice.feignClients;

import org.lear.aibotservice.models.CycleTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "cycletime-service",url = "gateway-service:8008/cycletime-service")
public interface CycleTimeClient {
    @GetMapping("/api/cycle-times")
    List<CycleTime> getAll();

    @GetMapping("/api/cycle-times/{id}")
    CycleTime getCycleTime(@PathVariable("id") Long id);

    @GetMapping("/api/cycle-times/machine/{machineId}")
    List<CycleTime> getByMachineId(@PathVariable("machineId") Long machineId);

    @GetMapping("/api/cycle-times/project/{projectId}")
    List<CycleTime> getByProjectId(@PathVariable("projectId") Long projectId);

}
