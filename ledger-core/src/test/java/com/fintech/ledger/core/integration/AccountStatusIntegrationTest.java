package com.fintech.ledger.core.integration;

import com.fintech.common.domain.IdempotencyKey;
import com.fintech.common.domain.Money;
import com.fintech.common.exception.AccountClosedException;
import com.fintech.common.exception.AccountFrozenException;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.repository.AccountRepository;
import com.fintech.ledger.core.service.TransactionEngine;
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
class AccountStatusIntegrationTest {

    @Autowired
    private TransactionEngine transactionEngine;

    @Autowired
    private AccountRepository accountRepository;

    private Account frozenAccount;
    private Account closedAccount;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        frozenAccount = new Account();
        frozenAccount.setName("Frozen Account");
        frozenAccount.setBalance(Money.of(new BigDecimal("1000.00"), "USD"));
        frozenAccount.setStatus(Account.AccountStatus.FROZEN);
        frozenAccount = accountRepository.save(frozenAccount);

        closedAccount = new Account();
        closedAccount.setName("Closed Account");
        closedAccount.setBalance(Money.of(new BigDecimal("500.00"), "USD"));
        closedAccount.setStatus(Account.AccountStatus.CLOSED);
        closedAccount = accountRepository.save(closedAccount);

        activeAccount = new Account();
        activeAccount.setName("Active Account");
        activeAccount.setBalance(Money.of(new BigDecimal("2000.00"), "USD"));
        activeAccount.setStatus(Account.AccountStatus.ACTIVE);
        activeAccount = accountRepository.save(activeAccount);
    }

    @Test
    void testFrozenAccountRejectsTransactions() {
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(frozenAccount.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("100.00"), "USD")),
            new TransactionEngine.LegRequest(activeAccount.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("100.00"), "USD"))
        );

        assertThrows(AccountFrozenException.class, () -> {
            transactionEngine.postTransaction(key, "Transaction on frozen account", legs, null);
        });
    }

    @Test
    void testClosedAccountRejectsTransactions() {
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(closedAccount.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("100.00"), "USD")),
            new TransactionEngine.LegRequest(activeAccount.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("100.00"), "USD"))
        );

        assertThrows(AccountClosedException.class, () -> {
            transactionEngine.postTransaction(key, "Transaction on closed account", legs, null);
        });
    }

    @Test
    void testActiveAccountAllowsTransactions() {
        IdempotencyKey key = IdempotencyKey.generate();
        List<TransactionEngine.LegRequest> legs = List.of(
            new TransactionEngine.LegRequest(activeAccount.getId(), TransactionLine.Type.DEBIT, 
                Money.of(new BigDecimal("100.00"), "USD")),
            new TransactionEngine.LegRequest(activeAccount.getId(), TransactionLine.Type.CREDIT, 
                Money.of(new BigDecimal("100.00"), "USD"))
        );

        Long journalId = transactionEngine.postTransaction(key, "Transaction on active account", legs, null);
        assertNotNull(journalId);
    }
}


