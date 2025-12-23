package com.fintech.clearing.saga;

import com.fintech.clearing.interfaces.WalletClient;
import com.fintech.clearing.interfaces.FraudClient;
import com.fintech.clearing.interfaces.LedgerClient;
import com.fintech.common.domain.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Lombok Slf4j needs dependency or manual logger

@RequiredArgsConstructor
public class TransferSagaCoordinator {

    private final WalletClient walletClient;
    private final FraudClient fraudClient;
    private final LedgerClient ledgerClient;

    /**
     * Orchestrates the transfer saga:
     * 1. Reserve funds (Wallet)
     * 2. Verify fraud (Anti-Fraud)
     * 3. Commit transaction (Ledger)
     * 
     * If any step fails, roll back previous steps.
     */
    public void executeTransfer(String fromAccount, String toAccount, Money amount, String userId) {
        // Step 1: Reserve Funds
        boolean reserved = walletClient.reserveFunds(fromAccount, amount);
        if (!reserved) {
            throw new RuntimeException("Transfer Failed: Could not reserve funds");
        }

        // Step 2: Verify Fraud
        boolean verified;
        try {
            verified = fraudClient.verifyTransaction(userId, amount, toAccount);
        } catch (Exception e) {
            // Unexpected error during verification -> Compensate Step 1
            walletClient.releaseFunds(fromAccount, amount);
            throw new RuntimeException("Transfer Failed: Fraud check error", e);
        }

        if (!verified) {
            // Fraud detected -> Compensate Step 1
            walletClient.releaseFunds(fromAccount, amount);
            throw new RuntimeException("Transfer Failed: Fraud detected");
        }

        // Step 3: Commit to Ledger
        try {
            ledgerClient.commitTransaction(fromAccount, toAccount, amount);
        } catch (Exception e) {
            // Ledger commit failed -> Compensate Step 1 (Funds released, transaction
            // aborted)
            walletClient.releaseFunds(fromAccount, amount);
            throw new RuntimeException("Transfer Failed: Ledger commit error", e);
        }

        // Success
    }
}
