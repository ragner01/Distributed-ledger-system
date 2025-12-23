package com.fintech.ledger.core.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple Token Bucket / Counter Rate Limiter.
 * Limit: 100 requests per minute per IP.
 */
@Component
@Slf4j
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE_IP = 100;
    private static final int MAX_REQUESTS_PER_MINUTE_USER = 200;
    private static final long CLEANUP_INTERVAL_MS = 300000; // 5 minutes
    private final Map<String, RequestCounter> ipRequestCounts = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> userRequestCounts = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = httpRequest.getRemoteAddr();
        String userId = getUserId();

        // Periodic cleanup to prevent memory leak
        cleanupOldEntries();

        // Check IP-based rate limit
        if (isRateLimited(clientIp, ipRequestCounts, MAX_REQUESTS_PER_MINUTE_IP)) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Too Many Requests: IP rate limit exceeded");
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return;
        }

        // Check user-based rate limit (if authenticated)
        if (userId != null && isRateLimited(userId, userRequestCounts, MAX_REQUESTS_PER_MINUTE_USER)) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Too Many Requests: User rate limit exceeded");
            log.warn("Rate limit exceeded for user: {}", userId);
            return;
        }

        chain.doFilter(request, response);
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    private boolean isRateLimited(String identifier, Map<String, RequestCounter> requestCounts, int maxRequests) {
        long currentMinute = System.currentTimeMillis() / 60000;
        requestCounts.computeIfAbsent(identifier, k -> new RequestCounter(currentMinute));

        RequestCounter counter = requestCounts.get(identifier);

        // Reset if new minute
        if (counter.minute != currentMinute) {
            counter.minute = currentMinute;
            counter.count.set(0);
        }

        return counter.count.incrementAndGet() > maxRequests;
    }

    /**
     * Cleanup entries older than 5 minutes to prevent memory leak
     */
    private void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime < CLEANUP_INTERVAL_MS) {
            return;
        }

        long currentMinute = now / 60000;
        long cutoffMinute = currentMinute - 5; // Remove entries older than 5 minutes

        ipRequestCounts.entrySet().removeIf(entry -> {
            RequestCounter counter = entry.getValue();
            return counter.minute < cutoffMinute;
        });

        userRequestCounts.entrySet().removeIf(entry -> {
            RequestCounter counter = entry.getValue();
            return counter.minute < cutoffMinute;
        });

        lastCleanupTime = now;
        log.debug("Cleaned up old rate limit entries. IP entries: {}, User entries: {}", 
            ipRequestCounts.size(), userRequestCounts.size());
    }

    private static class RequestCounter {
        volatile long minute;
        final AtomicInteger count = new AtomicInteger(0);

        RequestCounter(long minute) {
            this.minute = minute;
        }
    }
}
