package com.ecomm.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1 — Extract token from request header
        String token = extractTokenFromRequest(request);

        // Step 2 — If no token, skip this filter (public endpoints)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Step 3 — Extract email from token
            String email = jwtUtil.extractEmail(token);

            // Step 4 — If email found and no auth set in context yet
            if (StringUtils.hasText(email)
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                // Step 5 — Load user from DB
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // Step 6 — Validate token against loaded user
                if (jwtUtil.validateAccessToken(token, userDetails)) {

                    // Step 7 — Create auth object and set in Security Context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                          // credentials not needed after auth
                                    userDetails.getAuthorities()   // roles
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Step 8 — Tell Spring Security this request is authenticated
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug("Authenticated user: {}", email);
                }
            }

        } catch (Exception e) {
            log.error("Cannot authenticate user: {}", e.getMessage());
            // Don't throw — just let it fall through as unauthenticated
        }

        // Step 9 — Continue to next filter / controller
        filterChain.doFilter(request, response);
    }

    // Extract "Bearer <token>" from Authorization header
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // remove "Bearer " prefix
        }
        return null;
    }
}