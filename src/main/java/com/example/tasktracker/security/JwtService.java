package com.example.tasktracker.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;


@Service
public class JwtService {
    private final JwtEncoder encoder;

    public JwtService(@Value("${security.jwt.secret}") String secret){
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
        this.encoder=new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }
    public String issue(String username, List<String> roles){
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("task-tracker")
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .subject(username)
                .claim("roles",roles)
                .build();

        var header = org.springframework.security.oauth2.jwt.JwsHeader
                .with(MacAlgorithm.HS256)
                .build();
        return encoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
    }
}
