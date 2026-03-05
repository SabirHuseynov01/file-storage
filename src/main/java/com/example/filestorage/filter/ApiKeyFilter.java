package com.example.filestorage.filter;

import com.example.filestorage.apikey.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    private static final String KEY_CREATION_PATH = "/api/keys";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
            if (path.equals(KEY_CREATION_PATH) && request.getMethod().equalsIgnoreCase("POST")) {
                filterChain.doFilter(request, response);
                return;
        }
            String apiKey = request.getHeader("X-Api-Key");
            if (!apiKeyService.isValidKey(apiKey)){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API Key\"}");
                return;
            }

            filterChain.doFilter(request, response);
    }
}
