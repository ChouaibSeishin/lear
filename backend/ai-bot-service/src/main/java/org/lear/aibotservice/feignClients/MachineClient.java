package org.lear.aibotservice.feignClients;

import org.lear.aibotservice.models.Machine;
import org.lear.aibotservice.models.ProductionLine;
import org.lear.aibotservice.models.Step;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "machine-service",url = "gateway-service:8008/machine-service")

public interface MachineClient {
    @GetMapping("/api/machines")
    List<Machine> getAllMachines();

    @GetMapping("/api/machines/{id}")
    List<Machine> getMachine();

    @GetMapping("/api/steps")
    List<Step> getAllSteps();
    @GetMapping("/api/production-lines")
    List<ProductionLine> getAllProductionLines();




    @GetMapping("/api/machines/{id}")
    Machine getMachine(@PathVariable("id") Long id);



    @GetMapping("/api/production-lines/{id}")
    ProductionLine getProductionLine(@PathVariable("id") Long id);


    @GetMapping("/api/steps/{id}")
    Step getStep(@PathVariable("id") Long id);

}
