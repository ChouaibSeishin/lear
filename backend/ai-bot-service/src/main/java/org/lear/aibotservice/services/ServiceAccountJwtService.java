package org.lear.aibotservice.services;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Service
public class ServiceAccountJwtService {

    private final JwtEncoder jwtEncoder;
    private volatile String serviceAccountJwt;
    private long expirationTimeMillis;

    public ServiceAccountJwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String getServiceAccountJwt() {
        if (serviceAccountJwt == null || System.currentTimeMillis() >= expirationTimeMillis) {
            synchronized (this) {
                if (serviceAccountJwt == null || System.currentTimeMillis() >= expirationTimeMillis) {
                    refreshServiceAccountJwt();
                }
            }
        }
        return serviceAccountJwt;
    }

    private void refreshServiceAccountJwt() {
        Instant now = Instant.now();
        long expirationMinutes = 180;


        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("self-ai-bot-service")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject("test@mail.com")
                .claim("roles", Collections.singletonList("ROLE_ADMIN"))
                .claim("authorities",Collections.singletonList("ROLE_ADMIN"))
                .claim("username", "test@mail.com")
                .build();

        JwtEncoderParameters params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                claimsSet
        );

        this.serviceAccountJwt = jwtEncoder.encode(params).getTokenValue();
        this.expirationTimeMillis = System.currentTimeMillis() + (expirationMinutes * 60 * 1000L) - 5000; // 5 seconds buffer
        System.out.println("Generated new service account JWT."+ getServiceAccountJwt());
    }
}
