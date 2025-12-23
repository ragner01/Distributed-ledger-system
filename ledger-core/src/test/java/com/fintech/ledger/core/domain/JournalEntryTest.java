package com.fintech.ledger.core.domain;

import com.fintech.common.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryTest {

    @Test
    void testValidationSuccess() {
        JournalEntry entry = new JournalEntry();
        Account acct1 = new Account();
        Account acct2 = new Account();

        TransactionLine debit = new TransactionLine();
        debit.setType(TransactionLine.Type.DEBIT);
        debit.setAmount(Money.of(new BigDecimal("100"), "USD"));
        debit.setAccount(acct1);

        TransactionLine credit = new TransactionLine();
        credit.setType(TransactionLine.Type.CREDIT);
        credit.setAmount(Money.of(new BigDecimal("100"), "USD"));
        credit.setAccount(acct2);

        entry.addLine(debit);
        entry.addLine(credit);

        assertDoesNotThrow(entry::validate);
    }

    @Test
    void testValidationUnbalanced() {
        JournalEntry entry = new JournalEntry();

        TransactionLine debit = new TransactionLine();
        debit.setType(TransactionLine.Type.DEBIT);
        debit.setAmount(Money.of(new BigDecimal("100"), "USD"));

        TransactionLine credit = new TransactionLine();
        credit.setType(TransactionLine.Type.CREDIT);
        credit.setAmount(Money.of(new BigDecimal("99"), "USD")); // Mismatch

        entry.addLine(debit);
        entry.addLine(credit);

        IllegalStateException ex = assertThrows(IllegalStateException.class, entry::validate);
        assertTrue(ex.getMessage().contains("JournalEntry is not balanced"));
    }

    @Test
    void testValidationTooFewLines() {
        JournalEntry entry = new JournalEntry();
        TransactionLine debit = new TransactionLine();
        entry.addLine(debit);

        IllegalStateException ex = assertThrows(IllegalStateException.class, entry::validate);
        assertTrue(ex.getMessage().contains("must have at least 2 transaction lines"));
    }
}
