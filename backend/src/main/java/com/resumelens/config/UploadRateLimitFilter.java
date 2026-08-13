package com.resumelens.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Simple in-memory safety limit for local single-instance deployments. */
@Component
public class UploadRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MILLIS = 60_000;
    private final ResumeLensProperties properties;
    private final ObjectMapper json;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public UploadRateLimitFilter(ResumeLensProperties properties, ObjectMapper json) { this.properties = properties; this.json = json; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod()) || !"/api/resumes/analyze".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String client = request.getRemoteAddr(); long now = System.currentTimeMillis(); int limit = Math.max(1, properties.rateLimitPerMinute());
        Window window = windows.compute(client, (ignored, previous) -> previous == null || now - previous.openedAt() >= WINDOW_MILLIS ? new Window(now) : previous);
        if (window.count().incrementAndGet() > limit) {
            response.setStatus(429); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            json.writeValue(response.getWriter(), Map.of("timestamp", Instant.now().toString(), "status", "error", "message", "Too many analysis requests. Please wait a minute and try again.", "path", request.getRequestURI()));
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long openedAt, AtomicInteger count) { Window(long openedAt) { this(openedAt, new AtomicInteger()); } }
}
