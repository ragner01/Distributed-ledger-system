package com.fintech.ledger.core.controller;

import com.fintech.common.api.ApiResponse;
import com.fintech.common.domain.Money;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.dto.request.CreateAccountRequest;
import com.fintech.ledger.core.dto.response.AccountResponse;
import com.fintech.ledger.core.repository.AccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountRepository accountRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("Creating account: {}", request.getName());
        
        Account account = new Account();
        account.setName(request.getName());
        account.setBalance(Money.of(request.getInitialBalance(), request.getCurrencyCode()));
        
        Account saved = accountRepository.save(account);
        AccountResponse response = AccountResponse.from(saved);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new com.fintech.common.exception.AccountNotFoundException(id));
        
        return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(account)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> listAccounts(Pageable pageable) {
        Page<AccountResponse> accounts = accountRepository.findAll(pageable)
            .map(AccountResponse::from);
        
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }
}



