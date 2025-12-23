package com.fintech.ledger.core.integration;

import com.fintech.common.domain.IdempotencyKey;
import com.fintech.common.domain.Money;
import com.fintech.common.exception.DuplicateTransactionException;
import com.fintech.common.exception.InsufficientFundsException;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.repository.AccountRepository;
import com.fintech.ledger.core.service.TransactionEngine;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private TransactionEngine transactionEngine;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        account1 = new Account();
        account1.setName("Account 1");
        account1.setBalance(Money.of(new BigDecimal("1000.00"), "USD"));
        account1 = accountRepository.save(account1);

        account2 = new Account();
        account2.setName("Account 2");
        account2.setBalance(Money.of(new BigDecimal("500.00"), "USD"));
        account2 = accountRepository.save(account2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSuccessfulTransaction() {
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(account1.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("100.00"), "USD")),
            new TransactionEngine.LegRequest(account2.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("100.00"), "USD"))
        );

        Long journalId = transactionEngine.postTransaction(key, "Test Transfer", legs, null);
        
        assertNotNull(journalId);
        
        // Verify balances updated
        Account updated1 = accountRepository.findById(account1.getId()).orElseThrow();
        Account updated2 = accountRepository.findById(account2.getId()).orElseThrow();
        
        assertEquals(0, updated1.getBalanceAmount().compareTo(new BigDecimal("900.00")));
        assertEquals(0, updated2.getBalanceAmount().compareTo(new BigDecimal("600.00")));
    }

    @Test
    void testIdempotencyPreventsDuplicates() {
        IdempotencyKey key = IdempotencyKey.of("test-key-123");
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(account1.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("50.00"), "USD")),
            new TransactionEngine.LegRequest(account2.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("50.00"), "USD"))
        );

        // First transaction succeeds
        Long journalId1 = transactionEngine.postTransaction(key, "First", legs, null);
        assertNotNull(journalId1);

        // Second transaction with same key should fail
        assertThrows(DuplicateTransactionException.class, () -> {
            transactionEngine.postTransaction(key, "Duplicate", legs, null);
        });

        // Verify balance only changed once
        Account updated1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertEquals(0, updated1.getBalanceAmount().compareTo(new BigDecimal("950.00")));
    }

    @Test
    void testInsufficientFunds() {
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(account1.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("2000.00"), "USD")), // More than balance
            new TransactionEngine.LegRequest(account2.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("2000.00"), "USD"))
        );

        assertThrows(InsufficientFundsException.class, () -> {
            transactionEngine.postTransaction(key, "Overdraft Attempt", legs, null);
        });

        // Verify balance unchanged
        Account unchanged = accountRepository.findById(account1.getId()).orElseThrow();
        assertEquals(0, unchanged.getBalanceAmount().compareTo(new BigDecimal("1000.00")));
    }
}


