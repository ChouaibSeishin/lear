package org.lear.gatewayservice.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.lear.gatewayservice.service.JwtService;

import java.util.Objects;

@Component("AuthenticationFilter")
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtService jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Allow login/register endpoints
            if (request.getURI().getPath().contains("/auth")) {
                return chain.filter(exchange);
            }

            // Allow swagger resources
            if (request.getURI().getPath().matches(".*(/swagger-ui|/swagger-ui.html|/v3/api-docs|/swagger-resources|/webjars).*")) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return unauthorized(exchange, "Missing Authorization Header");
            }

            String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION).replace("Bearer ", "");
            System.out.println("Token extracted from Authorization header: " + token);

            try {
                jwtUtil.validateToken(token);
            } catch (Exception e) {
                return unauthorized(exchange, "Invalid JWT: " + e.getMessage());
            }

            // Forward the token to the next service (User Service)
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();

            System.out.println("Authorization header forwarded to downstream service: " + "Bearer " + token);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }


    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}
