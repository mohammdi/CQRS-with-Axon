package com.example.cqrs.config;

import com.example.cqrs.command.services.CommandIdempotencyService;
import com.example.cqrs.command.services.CommandIdempotencyService.AlreadyHandledException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    @Autowired
    private CommandIdempotencyService idempotencyService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Filter only POST and PUT to /api/command/* endpoints
        String path = request.getRequestURI();
        return !("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()))
                || !path.startsWith("/api/command/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(res);
        String requestId = request.getHeader("x-request-id");
        if (requestId == null || requestId.isBlank()) {
            response.setStatus(400);
            response.setContentType("text/plain");
            response.getWriter().write("Missing required x-request-id header");
            response.copyBodyToResponse();
            return;
        }
        try {
            idempotencyService.ensureIdempotencyOrThrow(requestId);
            chain.doFilter(request, response);
            // After success, parse fields for logging
            String json = new String(request.getContentAsByteArray().length > 0 ? request.getContentAsByteArray() : request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String accountNumber = null, referenceNumber = null, commandName = null;
            if (!json.isBlank()) {
                JsonNode node = objectMapper.readTree(json);
                if (node.has("accountNumber")) accountNumber = node.get("accountNumber").asText();
                if (node.has("referenceNumber")) referenceNumber = node.get("referenceNumber").asText();
                if (node.has("commandName")) commandName = node.get("commandName").asText();
                if (commandName == null && request.getRequestURI() != null) {
                    if (request.getRequestURI().contains("deposit")) commandName = "DepositCommand";
                    else if (request.getRequestURI().contains("withdraw")) commandName = "WithdrawCommand";
                    else if (request.getRequestURI().contains("create")) commandName = "CreateAccountCommand";
                    else commandName = "UnknownCommand";
                }
                if (referenceNumber == null && "CreateAccountCommand".equals(commandName)) referenceNumber = accountNumber;
            }
            idempotencyService.logCommand(commandName, accountNumber, requestId, referenceNumber);
        } catch (CommandIdempotencyService.AlreadyHandledException ex) {
            response.setStatus(409);
            response.setContentType("text/plain");
            response.getWriter().write("This requestId is done before");
        } finally {
            response.copyBodyToResponse();
        }
    }
}
