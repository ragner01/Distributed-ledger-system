-- Additional constraints and indexes for performance and data integrity

-- Add constraint to ensure journal entries have at least 2 lines (enforced at application level, but good to document)
-- Note: This is enforced by application logic, but we add a comment here

COMMENT ON TABLE journal_entries IS 'Journal entries must have at least 2 transaction lines (double-entry bookkeeping requirement)';
COMMENT ON TABLE transaction_lines IS 'Each transaction line must reference a valid account and journal entry';
COMMENT ON TABLE accounts IS 'Account balances must be non-negative. Status can be ACTIVE, FROZEN, or CLOSED';

-- Add partial index for active accounts (most common query)
CREATE INDEX IF NOT EXISTS idx_accounts_active ON accounts(id) WHERE status = 'ACTIVE';

-- Add index for recent journal entries (common query pattern)
CREATE INDEX IF NOT EXISTS idx_journal_entries_recent ON journal_entries(timestamp DESC) WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days';

-- Add composite index for transaction line queries by account and type
CREATE INDEX IF NOT EXISTS idx_transaction_lines_account_type ON transaction_lines(account_id, type);

-- Add index for audit logs error queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_errors ON audit_logs(timestamp DESC) WHERE error_message IS NOT NULL;



