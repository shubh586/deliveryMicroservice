package com.foodexpress.gateway.filter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * global filter to vali  tokens and add user info to headers
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/oauth2/",
            "/login/oauth2/",
            "/actuator/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_EMAIL_HEADER);
                    headers.remove(USER_ROLE_HEADER);
                })
                .build();
        String path = request.getURI().getPath();
        ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

        if (isPublicPath(request)) {
            return chain.filter(mutatedExchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return onError(mutatedExchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {

            Claims claims = validateToken(token);


            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(USER_ID_HEADER, claims.getSubject())
                    .header(USER_EMAIL_HEADER, claims.get("email", String.class))
                    .header(USER_ROLE_HEADER, claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            return onError(mutatedExchange, "Token expired", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException | MalformedJwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return onError(mutatedExchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error validating JWT token", e);
            return onError(mutatedExchange, "Authentication error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        request.getMethod();
        String method = request.getMethod().name();
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }
        if (path.startsWith("/api/restaurants") && "GET".equalsIgnoreCase(method)) {
            return !path.startsWith("/api/restaurants/my");
        }
        return false;
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"success\":false,\"message\":\"%s\"}", message);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -100; // High priority
    }
}

