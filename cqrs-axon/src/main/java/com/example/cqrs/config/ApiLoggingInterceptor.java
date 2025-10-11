package com.example.cqrs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.nio.charset.StandardCharsets;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            params.append(name).append("=").append(request.getParameter(name)).append(" ");
        }
        String body = null;
        if (request instanceof ContentCachingRequestWrapper wrapper &&
            ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()))) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0 && buf.length < 10_000) {
                body = new String(buf, StandardCharsets.UTF_8);
            }
        }
        logger.info("Incoming API call: [{} {}] Params: {}{}", request.getMethod(), request.getRequestURI(), params, body != null ? ", Body: " + body : "");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            logger.info("Completed API call: [{} {}] Status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
            return;
        }
        String responseBody = null;
        String requestBody = null;
        if (response instanceof ContentCachingResponseWrapper responseWrapper) {
            byte[] buf = responseWrapper.getContentAsByteArray();
            if (buf.length > 0 && buf.length < 10_000) {
                responseBody = new String(buf, StandardCharsets.UTF_8);
            }
        }
        if (request instanceof ContentCachingRequestWrapper requestWrapper) {
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0 && buf.length < 10_000) {
                requestBody = new String(buf, StandardCharsets.UTF_8);
            }
        }
        logger.info("Completed API call: [{} {}] Status: {}{}{}", request.getMethod(), request.getRequestURI(), response.getStatus(),
            requestBody != null ? ", RequestBody: " + requestBody : "",
            responseBody != null ? ", ResponseBody: " + responseBody : "");
    }
}
