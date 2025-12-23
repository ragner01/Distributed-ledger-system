package com.fintech.ledger.core.service;

import com.fintech.common.audit.Auditable;
import com.fintech.common.domain.Money;
import com.fintech.ledger.core.domain.TransactionLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrossBorderTransferService {

    private final TransactionEngine transactionEngine;
    private final ExchangeRateService exchangeRateService;

    // Hardcoded FX Desk Accounts for assumption (In real app, fetch from config)
    private static final Long FX_DESK_SOURCE_ACCT_ID = 998L;
    private static final Long FX_DESK_TARGET_ACCT_ID = 999L;

    /**
     * Executes a Cross-Border Transfer.
     * 1. Debit Source User (Source Currency)
     * 2. Credit FX Desk (Source Currency)
     * 3. Debit FX Desk (Target Currency)
     * 4. Credit Target User (Target Currency)
     */
    @Auditable(action = "CROSS_BORDER_TRANSFER")
    public void executeTransfer(Long sourceAcctId, Long targetAcctId, BigDecimal amount, String sourceCurrency,
            String targetCurrency) {
        log.info("Initiating FX Transfer: {} {} -> {}", amount, sourceCurrency, targetCurrency);

        BigDecimal rate = exchangeRateService.getRate(sourceCurrency, targetCurrency);
        BigDecimal targetAmountVal = amount.multiply(rate); // Precision handled by Money VO in engine

        Money sourceMoney = Money.of(amount, sourceCurrency);
        Money targetMoney = Money.of(targetAmountVal, targetCurrency);

        // Construct 4 Atomic Legs
        List<TransactionEngine.LegRequest> legs = List.of(
                // Leg 1: Debit User A (Source Ccy)
                new TransactionEngine.LegRequest(sourceAcctId, TransactionLine.Type.DEBIT, sourceMoney),
                // Leg 2: Credit FX Desk (Source Ccy) - "Bank buys source currency"
                new TransactionEngine.LegRequest(FX_DESK_SOURCE_ACCT_ID, TransactionLine.Type.CREDIT, sourceMoney),

                // Leg 3: Debit FX Desk (Target Ccy) - "Bank sells target currency"
                new TransactionEngine.LegRequest(FX_DESK_TARGET_ACCT_ID, TransactionLine.Type.DEBIT, targetMoney),
                // Leg 4: Credit User B (Target Ccy)
                new TransactionEngine.LegRequest(targetAcctId, TransactionLine.Type.CREDIT, targetMoney));

        transactionEngine.postTransaction("FX Transfer " + sourceCurrency + " to " + targetCurrency, legs);
        log.info("FX Transfer Completed.");
    }
}
