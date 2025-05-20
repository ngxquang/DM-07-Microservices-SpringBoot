package com.programming.ngxquang.api_gateway.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class GatewayCacheFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis(); // ⏱️ bắt đầu đếm thời gian

        String requestUri = request.getRequestURI();
        String cacheKey = "gateway::" + request.getRequestURI();

        if (requestUri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String cachedResponse = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            long duration = System.currentTimeMillis() - start;
            System.out.println("CACHE HIT for " + cacheKey + " (Time: " + duration + " ms)");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(cachedResponse);
            return;
        }

        // Nếu không có trong cache thì gọi tiếp
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrappedResponse);

        String body = new String(wrappedResponse.getContentAsByteArray(), response.getCharacterEncoding());
        redisTemplate.opsForValue().set(cacheKey, body, 60, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - start;
        System.out.println("CACHE MISS for " + cacheKey + " (Time: " + duration + " ms)");

        wrappedResponse.copyBodyToResponse();
    }

}

