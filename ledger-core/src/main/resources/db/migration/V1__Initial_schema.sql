-- Initial schema for Distributed Ledger System

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    balance_amount NUMERIC(30, 18) NOT NULL DEFAULT 0,
    balance_currency VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_balance_non_negative CHECK (balance_amount >= 0),
    CONSTRAINT chk_status_valid CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
);

CREATE INDEX idx_accounts_name ON accounts(name);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Journal Entries table
CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_journal_entries_timestamp ON journal_entries(timestamp);

-- Transaction Lines table
CREATE TABLE IF NOT EXISTS transaction_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount_value NUMERIC(30, 18) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT chk_type_valid CHECK (type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_amount_positive CHECK (amount_value > 0),
    CONSTRAINT fk_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_currency_code_length CHECK (LENGTH(currency_code) = 3)
);

CREATE INDEX idx_transaction_lines_journal_entry ON transaction_lines(journal_entry_id);
CREATE INDEX idx_transaction_lines_account ON transaction_lines(account_id);
CREATE INDEX idx_transaction_lines_type ON transaction_lines(type);
CREATE INDEX idx_transaction_lines_currency ON transaction_lines(currency_code);

-- Transaction Idempotency table
CREATE TABLE IF NOT EXISTS transaction_idempotency (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    journal_entry_id BIGINT NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_journal_entry_idempotency FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
);

CREATE INDEX idx_idempotency_key ON transaction_idempotency(idempotency_key);
CREATE INDEX idx_idempotency_processed_at ON transaction_idempotency(processed_at);

-- Audit Logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    user_name VARCHAR(255),
    ip_address VARCHAR(45),
    method_name VARCHAR(255),
    arguments TEXT,
    result TEXT,
    error_message TEXT,
    duration_ms BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trace_id VARCHAR(36)
);

CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_user ON audit_logs(user_name);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_trace_id ON audit_logs(trace_id);

-- User Transaction Limits table (for rate limiting and daily limits)
CREATE TABLE IF NOT EXISTS user_transaction_limits (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_count INTEGER NOT NULL DEFAULT 0,
    total_amount NUMERIC(30, 18) NOT NULL DEFAULT 0,
    currency_code VARCHAR(3) NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_date_currency UNIQUE (user_id, transaction_date, currency_code)
);

CREATE INDEX idx_user_limits_user_date ON user_transaction_limits(user_id, transaction_date);
CREATE INDEX idx_user_limits_date ON user_transaction_limits(transaction_date);



