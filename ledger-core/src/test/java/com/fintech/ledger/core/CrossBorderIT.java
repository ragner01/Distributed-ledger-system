package com.fintech.ledger.core;

import com.fintech.common.domain.Money;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.repository.JournalEntryRepository;
import com.fintech.ledger.core.service.CrossBorderTransferService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
@Transactional
public class CrossBorderIT {

    @Autowired
    private CrossBorderTransferService fxService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private EntityManager em;

    @Test
    void testCriticalPath_CrossBorderTransfer() {
        // 1. Setup Accounts
        Account userA = new Account();
        userA.setName("User A");
        userA.setBalance(Money.of(new BigDecimal("1000.00"), "EUR"));
        em.persist(userA);

        Account userB = new Account();
        userB.setName("User B");
        userB.setBalance(Money.of(new BigDecimal("0.00"), "USD"));
        em.persist(userB);

        Account fxEur = new Account();
        fxEur.setName("FX Desk EUR");
        fxEur.setBalance(Money.of(new BigDecimal("1000000.00"), "EUR"));
        em.persist(fxEur);

        Account fxUsd = new Account();
        fxUsd.setName("FX Desk USD");
        fxUsd.setBalance(Money.of(new BigDecimal("1000000.00"), "USD"));
        em.persist(fxUsd);

        em.flush();

        // 2. Execute Transfer: 100 EUR -> USD
        // Rate 1.10 -> Expecting 110 USD credited to B
        // Note: FX desk accounts are hardcoded in CrossBorderTransferService (998L, 999L)
        fxService.executeTransfer(userA.getId(), userB.getId(),
                new BigDecimal("100.00"), "EUR", "USD");

        em.flush();
        em.clear(); // Clear cache to fetch fresh data

        // 3. Verify Balances
        Account updatedA = em.find(Account.class, userA.getId());
        Account updatedB = em.find(Account.class, userB.getId());
        Account updatedFxEur = em.find(Account.class, fxEur.getId());
        Account updatedFxUsd = em.find(Account.class, fxUsd.getId());

        // A Balance: 1000 - 100 = 900 EUR
        Assertions.assertEquals(0, updatedA.getBalanceAmount().compareTo(new BigDecimal("900.00")));

        // B Balance: 0 + 110 = 110 USD
        Assertions.assertEquals(0, updatedB.getBalanceAmount().compareTo(new BigDecimal("110.000000000000000000"))); // Scale
                                                                                                                     // 18

        // FX EUR: 1M + 100
        Assertions.assertEquals(0, updatedFxEur.getBalanceAmount().compareTo(new BigDecimal("1000100.00")));

        // FX USD: 1M - 110
        Assertions.assertEquals(0, updatedFxUsd.getBalanceAmount().compareTo(new BigDecimal("999890.00")));

        System.out.println("CRITICAL PATH TEST PASSED: Cross-Border Transfer Verified.");
    }
}
