package com.example.demo.auth;

import com.example.demo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/login", "/register", "/uploads/**", "/api/uploads/**", 
            "/api/ai/**", "/ai/**", "/h2-console/**", "/ws/chat", "/api/ws/chat",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/levels", "/ranking", "/hot-questions", "/bilibili/**", "/courses/**",
            "/forum-posts/**"
    );

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }
    
    private boolean isPublicPath(String requestPath) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            if (isPublicPath(request.getRequestURI())) {
                filterChain.doFilter(request, response);
            } else {
                writeUnauthorized(response, "登录凭证无效");
            }
            return;
        }

        try {
            AuthenticatedUser authenticatedUser = jwtService.parseToken(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            System.err.println("[JWT Error] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            ex.printStackTrace();
            if (isPublicPath(request.getRequestURI())) {
                filterChain.doFilter(request, response);
            } else {
                writeUnauthorized(response, "登录凭证无效或已过期");
            }
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.fail(40101, message));
    }
}
