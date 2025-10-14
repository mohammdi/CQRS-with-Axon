package com.example.cqrs.command.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class GeneratorService {
    public String generateReferenceNumber() {
        return UUID.randomUUID().toString();
    }
    public String generateAccountNumber() {
        return RandomStringUtils.randomNumeric(10);
    }
}
