package org.lear.userservice.config;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8008/user-service");
        gatewayServer.setDescription("API Gateway through gateway-service");

        return new OpenAPI().servers(List.of(gatewayServer));
    }
}

