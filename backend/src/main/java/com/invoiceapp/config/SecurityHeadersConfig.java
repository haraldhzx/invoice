package com.invoiceapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {

                // Prevent clickjacking attacks
                response.setHeader("X-Frame-Options", "DENY");

                // Prevent MIME type sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");

                // Enable browser XSS protection
                response.setHeader("X-XSS-Protection", "1; mode=block");

                // Content Security Policy
                response.setHeader("Content-Security-Policy",
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");

                // Referrer Policy
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                // Permissions Policy (formerly Feature Policy)
                response.setHeader("Permissions-Policy",
                        "geolocation=(), " +
                        "microphone=(), " +
                        "camera=(), " +
                        "payment=(), " +
                        "usb=(), " +
                        "magnetometer=(), " +
                        "gyroscope=(), " +
                        "accelerometer=()");

                // Strict Transport Security (HSTS) - only for HTTPS
                if (request.isSecure()) {
                    response.setHeader("Strict-Transport-Security",
                            "max-age=31536000; includeSubDomains; preload");
                }

                // Prevent caching of sensitive data
                if (request.getRequestURI().contains("/api/auth") ||
                    request.getRequestURI().contains("/api/invoices") ||
                    request.getRequestURI().contains("/api/transactions")) {
                    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                }

                // Server header obfuscation
                response.setHeader("Server", "InvoiceApp");

                filterChain.doFilter(request, response);
            }
        };
    }
}
