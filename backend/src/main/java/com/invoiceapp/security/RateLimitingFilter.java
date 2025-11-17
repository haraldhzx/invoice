package com.invoiceapp.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key, request);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {}, path: {}", key, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            response.setContentType("application/json");
        }
    }

    private Bucket resolveBucket(String key, HttpServletRequest request) {
        return cache.computeIfAbsent(key, k -> createNewBucket(request));
    }

    private Bucket createNewBucket(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Different limits for different endpoints
        Bandwidth limit;
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            // Stricter limit for auth endpoints: 5 requests per minute
            limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        } else if (path.contains("/invoices/upload")) {
            // Moderate limit for upload: 10 requests per minute
            limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        } else {
            // General API limit: 100 requests per minute
            limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        }

        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientKey(HttpServletRequest request) {
        // Try to get authenticated user first
        String user = request.getRemoteUser();
        if (user != null) {
            return user;
        }

        // Fall back to IP address
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Don't rate limit health checks and static resources
        return path.contains("/actuator/health") ||
               path.contains("/swagger-ui") ||
               path.contains("/v3/api-docs");
    }
}
