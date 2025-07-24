package org.lear.importservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


    @FeignClient(name = "user-service",url = "gateway-service:8008/user-service")
    public interface UserServiceClient {
        @PostMapping("/auth/register")
        void createUser(@RequestBody Map<String, Object> body);

        @GetMapping("/api/users/{email}")
        Long getUserIdByEmail(@PathVariable String email);

        @PutMapping("/api/users")
        void updateUser(@RequestBody Map<String, Object> body);

    }

