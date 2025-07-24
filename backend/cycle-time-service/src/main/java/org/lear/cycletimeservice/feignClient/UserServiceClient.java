package org.lear.cycletimeservice.feignClient;


import org.lear.cycletimeservice.dtos.UserLogRequest;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "user-service",url = "gateway-service:8008/user-service")
public interface UserServiceClient {
    @PostMapping("/api/logs")
    void logUserAction(@RequestBody UserLogRequest logRequest);
}
