package com.example.cqrs.query.repositories;

import com.example.cqrs.domain.SavingsTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {
    List<SavingsTransaction> findByAccountNumberOrderByTransactionDateDesc(String accountNumber);
    List<SavingsTransaction> findByAccountNumberAndReferenceNumberOrderByTransactionDateDesc(String accountNumber, String referenceNumber);
}


