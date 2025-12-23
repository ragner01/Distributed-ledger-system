package com.fintech.ledger.core.integration;

import com.fintech.common.domain.IdempotencyKey;
import com.fintech.common.domain.Money;
import com.fintech.common.exception.TransactionLimitExceededException;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.repository.AccountRepository;
import com.fintech.ledger.core.service.TransactionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "ledger.transaction.limits.daily.count=5",
    "ledger.transaction.limits.daily.amount=1000.00"
})
@Transactional
class TransactionLimitIntegrationTest {

    @Autowired
    private TransactionEngine transactionEngine;

    @Autowired
    private AccountRepository accountRepository;

    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        account1 = new Account();
        account1.setName("Account 1");
        account1.setBalance(Money.of(new BigDecimal("10000.00"), "USD"));
        account1 = accountRepository.save(account1);

        account2 = new Account();
        account2.setName("Account 2");
        account2.setBalance(Money.of(new BigDecimal("10000.00"), "USD"));
        account2 = accountRepository.save(account2);
    }

    @Test
    void testTransactionCountLimit() {
        String userId = "test-user-123";
        
        // Make 5 transactions (at limit)
        for (int i = 0; i < 5; i++) {
            IdempotencyKey key = IdempotencyKey.generate();
            List<TransactionEngine.LegRequest> legs = List.of(
                new TransactionEngine.LegRequest(account1.getId(), TransactionLine.Type.DEBIT, 
                    Money.of(new BigDecimal("10.00"), "USD")),
                new TransactionEngine.LegRequest(account2.getId(), TransactionLine.Type.CREDIT, 
                    Money.of(new BigDecimal("10.00"), "USD"))
            );
            transactionEngine.postTransaction(key, "Transaction " + i, legs, userId);
        }

        // 6th transaction should fail
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(account1.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("10.00"), "USD")),
            new TransactionEngine.LegRequest(account2.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("10.00"), "USD"))
        );

        assertThrows(TransactionLimitExceededException.class, () -> {
            transactionEngine.postTransaction(key, "Transaction 6", legs, userId);
        });
    }

    @Test
    void testTransactionAmountLimit() {
        String userId = "test-user-456";
        
        // Make transaction that exceeds amount limit
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(account1.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("1500.00"), "USD")), // Exceeds 1000 limit
            new TransactionEngine.LegRequest(account2.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("1500.00"), "USD"))
        );

        assertThrows(TransactionLimitExceededException.class, () -> {
            transactionEngine.postTransaction(key, "Large transaction", legs, userId);
        });
    }
}


