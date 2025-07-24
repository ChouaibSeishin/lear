package org.lear.aibotservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.lear.aibotservice.services.ServiceAccountJwtService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class OAuth2RequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String GOOGLE_API_HOST = "generativelanguage.googleapis.com";

    private final ServiceAccountJwtService serviceAccountJwtService;

    public OAuth2RequestInterceptor(ServiceAccountJwtService serviceAccountJwtService) {
        this.serviceAccountJwtService = serviceAccountJwtService;
    }

    @Override
    public void apply(RequestTemplate template) {
        // Bypass for Google API if it doesn't need your internal JWT
        if (template.url() != null && template.url().contains(GOOGLE_API_HOST)) {
            return;
        }

        String jwtToApply = null;

        // 1. Attempt to retrieve JWT from the current HttpServletRequest context (user's request)
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes requestAttributes) {
            HttpServletRequest request = requestAttributes.getRequest();
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                jwtToApply = authHeader.substring(BEARER_PREFIX.length());
            }
        }

        // 2. If no user JWT found (e.g., at startup or background task), use the service account JWT
        if (jwtToApply == null) {
            jwtToApply = serviceAccountJwtService.getServiceAccountJwt();
        }

        // 3. Apply the determined JWT to the Feign request
        if (jwtToApply != null) {
            template.header(AUTHORIZATION_HEADER, BEARER_PREFIX + jwtToApply);
        } else {
            // Log a warning or throw an exception if a JWT is expected but none is found
            // This indicates a configuration issue or a call to a secured endpoint without a token.
            System.err.println("Warning: No JWT (user or service account) available for Feign call to " + template.url() +
                    ". This might lead to authorization failures.");
            // Depending on your security stance, you might want to throw a RuntimeException here
            // to fail fast if unauthenticated internal calls are strictly forbidden.
        }
    }
}
