package com.Hotel.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Yeh paths JWT check se skip honge
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/signup",
            "/auth/login",
            "/auth/verify-otp",
            "/auth/resend-otp",
            "/public/",
            "/swagger-ui",       // ← ADD
            "/v3/api-docs",      // ← ADD
            "/swagger-resources"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Public paths skip karo — JWT check nahi
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Authorization header check karo
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header: {}", path);
            sendUnauthorized(response, "Missing Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        // Token validate karo
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            sendUnauthorized(response, "Invalid or expired token");
            return;
        }

        // Token valid hai — user info headers mein add karo
        // Downstream services in headers se user info lenge
        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String roles = jwtUtil.getRolesFromToken(token);

        // Request mein headers inject karo
        CustomRequestWrapper wrappedRequest =
                new CustomRequestWrapper(request, username, userId, roles);
        log.info("JWT valid — user: {} path: {}", username, path);
        chain.doFilter(wrappedRequest, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private void sendUnauthorized(HttpServletResponse response,
                                  String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + message + "\"}"
        );
    }
}