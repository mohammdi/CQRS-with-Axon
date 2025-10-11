package com.example.cqrs.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class RequestBodyCachingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ServletRequest reqToUse = request;
        ServletResponse resToUse = response;
        if (request instanceof HttpServletRequest && !(request instanceof ContentCachingRequestWrapper)) {
            reqToUse = new ContentCachingRequestWrapper((HttpServletRequest) request);
        }
        if (response instanceof HttpServletResponse && !(response instanceof ContentCachingResponseWrapper)) {
            resToUse = new ContentCachingResponseWrapper((HttpServletResponse) response);
        }
        chain.doFilter(reqToUse, resToUse);
        // Make sure response body is copied back
        if (resToUse instanceof ContentCachingResponseWrapper wrapper) {
            wrapper.copyBodyToResponse();
        }
    }
}
