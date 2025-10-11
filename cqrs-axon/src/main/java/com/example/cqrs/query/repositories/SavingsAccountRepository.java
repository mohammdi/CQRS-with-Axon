package com.example.cqrs.query.repositories;

import com.example.cqrs.domain.SavingsAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
    Optional<SavingsAccount> findByAccountNumber(String accountNumber);
}


