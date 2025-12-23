# Operational Runbook

## 🚨 Critical Alerts & Response Procedures

### 1. System Halted (Reconciliation Failure)

**Symptoms**:
- Health check returns DOWN
- Logs show: "CRITICAL: Ledger Integrity Failure"
- All transactions rejected

**Immediate Actions**:
1. **STOP ALL TRANSACTIONS** - System is automatically halted
2. Check reconciliation logs: `SELECT * FROM audit_logs WHERE action = 'RECONCILIATION' ORDER BY timestamp DESC LIMIT 10`
3. Identify the account with mismatch:
   ```sql
   SELECT a.id, a.name, a.balance_amount, 
          SUM(CASE WHEN tl.type = 'DEBIT' THEN -tl.amount_value ELSE tl.amount_value END) as calculated
   FROM accounts a
   LEFT JOIN transaction_lines tl ON tl.account_id = a.id
   GROUP BY a.id, a.name, a.balance_amount
   HAVING a.balance_amount != SUM(...)
   ```
4. **DO NOT** manually fix balances without investigation
5. Contact on-call engineer and finance team
6. Preserve audit logs for investigation

**Recovery Steps**:
1. Investigate root cause (check recent transactions, audit logs)
2. If legitimate discrepancy: Create correction journal entry
3. If data corruption: Restore from backup
4. Re-run reconciliation manually: `POST /actuator/reconciliation` (if endpoint exists)
5. Once fixed, restart application to clear halt flag

---

### 2. High Error Rate Detected

**Symptoms**:
- Health check returns WARNING or DOWN
- Metrics show: `ledger.transactions.errors` increasing
- Error rate > 50/hour (WARNING) or > 100/hour (DOWN)

**Immediate Actions**:
1. Check recent errors: `SELECT * FROM audit_logs WHERE error_message IS NOT NULL ORDER BY timestamp DESC LIMIT 50`
2. Group by error type:
   ```sql
   SELECT error_message, COUNT(*) as count 
   FROM audit_logs 
   WHERE error_message IS NOT NULL 
     AND timestamp > NOW() - INTERVAL '1 hour'
   GROUP BY error_message
   ORDER BY count DESC
   ```
3. Check for patterns:
   - Specific user causing errors?
   - Specific account?
   - Specific transaction type?

**Common Causes**:
- **InsufficientFundsException**: User trying to spend more than balance
- **CurrencyMismatchException**: Wrong currency in transaction
- **AccountFrozenException**: Account frozen, transactions blocked
- **TransactionLimitExceededException**: User hit daily limits

**Resolution**:
- If legitimate errors: No action needed (users need to fix their requests)
- If system error: Check application logs, database connectivity
- If fraud attempt: Freeze account, alert security team

---

### 3. Transaction Limit Exceeded

**Symptoms**:
- Users reporting "Transaction limit exceeded" errors
- `TransactionLimitExceededException` in logs

**Investigation**:
```sql
SELECT user_id, transaction_date, transaction_count, total_amount, currency_code
FROM user_transaction_limits
WHERE transaction_date = CURRENT_DATE
ORDER BY transaction_count DESC
```

**Actions**:
1. Verify if legitimate high-volume user or abuse
2. Check user's transaction history
3. If legitimate: Consider increasing limits temporarily
4. If abuse: Freeze account, investigate further

**Temporary Limit Increase**:
- Update `application.properties`: `ledger.transaction.limits.daily.count=200`
- Restart application (or use feature flag if implemented)

---

### 4. Duplicate Transaction Attempts

**Symptoms**:
- `DuplicateTransactionException` in logs
- Same idempotency key used multiple times

**Investigation**:
```sql
SELECT idempotency_key, journal_entry_id, processed_at, COUNT(*) as attempts
FROM transaction_idempotency
WHERE processed_at > NOW() - INTERVAL '1 hour'
GROUP BY idempotency_key, journal_entry_id, processed_at
HAVING COUNT(*) > 1
```

**Actions**:
1. Check if client is retrying failed requests (normal behavior)
2. Verify original transaction succeeded:
   ```sql
   SELECT * FROM journal_entries WHERE id = <journal_entry_id>
   ```
3. If original succeeded: Return success response to client
4. If original failed: Allow retry (shouldn't happen with idempotency)

---

### 5. Account Status Issues

**Symptoms**:
- `AccountFrozenException` or `AccountClosedException` errors
- Users cannot process transactions

**Investigation**:
```sql
SELECT id, name, status, balance_amount, balance_currency
FROM accounts
WHERE status IN ('FROZEN', 'CLOSED')
```

**Actions**:
- **Frozen Account**: Usually intentional (fraud investigation, compliance)
  - Verify reason for freeze
  - Contact account owner if needed
  - Unfreeze when appropriate: `UPDATE accounts SET status = 'ACTIVE' WHERE id = ?`
  
- **Closed Account**: Permanent closure
  - Verify closure is intentional
  - Ensure balance is zero or transferred
  - Do not reopen without proper authorization

---

### 6. Database Connection Issues

**Symptoms**:
- Health check fails
- Transaction errors
- Connection pool exhaustion

**Immediate Actions**:
1. Check database connectivity: `SELECT 1`
2. Check connection pool metrics: `/actuator/metrics/hikari.connections.active`
3. Check for long-running transactions:
   ```sql
   SELECT pid, usename, application_name, state, query_start, query
   FROM pg_stat_activity
   WHERE state = 'active'
   ORDER BY query_start
   ```
4. Kill long-running queries if needed: `SELECT pg_terminate_backend(pid)`

**Resolution**:
- Increase connection pool if needed
- Optimize slow queries
- Check database server resources

---

### 7. Rate Limiting Issues

**Symptoms**:
- Users getting 429 (Too Many Requests) errors
- Legitimate users blocked

**Investigation**:
- Check rate limit metrics
- Review user activity patterns
- Check if DDoS attack or legitimate high volume

**Actions**:
- If legitimate: Temporarily increase limits
- If attack: Block IP addresses, contact security team
- Review rate limit configuration

---

## 📊 Monitoring & Metrics

### Key Metrics to Monitor

1. **Transaction Metrics**:
   - `ledger.transactions.total` - Total transactions
   - `ledger.transactions.errors` - Failed transactions
   - `ledger.transactions.duplicates` - Duplicate attempts
   - `ledger.transactions.processing.time` - Processing latency

2. **Reconciliation Metrics**:
   - `ledger.reconciliation.total` - Reconciliation runs
   - `ledger.reconciliation.failures` - Reconciliation failures

3. **System Metrics**:
   - Health check status
   - Error rate (last hour)
   - Database connection pool
   - Response times

### Alert Thresholds

- **CRITICAL**: System halted, reconciliation failure
- **HIGH**: Error rate > 100/hour, database connection issues
- **MEDIUM**: Error rate > 50/hour, high latency
- **LOW**: Elevated error rate, rate limit hits

---

## 🔧 Maintenance Procedures

### Daily Checks

1. Review error logs
2. Check reconciliation status
3. Monitor transaction volumes
4. Review rate limit hits

### Weekly Checks

1. Review audit logs for anomalies
2. Check account status changes
3. Review transaction limit usage
4. Database performance review

### Monthly Checks

1. Audit log retention (compliance)
2. Database backup verification
3. Security review
4. Performance optimization review

---

## 📞 Escalation Contacts

- **On-Call Engineer**: [Contact Info]
- **Finance Team**: [Contact Info]
- **Security Team**: [Contact Info]
- **Database Admin**: [Contact Info]

---

## 🔐 Security Procedures

### Suspected Fraud

1. Freeze affected accounts immediately
2. Preserve audit logs
3. Contact security team
4. Document incident

### Data Breach

1. Isolate affected systems
2. Preserve logs and evidence
3. Contact security and legal teams
4. Follow incident response plan

---

## 📝 Log Locations

- **Application Logs**: `/var/log/fintech/ledger-core.log`
- **Audit Logs**: `audit_logs` table in database
- **Metrics**: Prometheus at `http://localhost:9090`
- **Health Checks**: `/actuator/health`

---

## 🚀 Deployment Procedures

### Pre-Deployment

1. Run all tests: `./gradlew test`
2. Verify Flyway migrations: `./gradlew flywayInfo`
3. Review changelog
4. Backup database

### Deployment

1. Deploy to staging first
2. Run smoke tests
3. Monitor for 30 minutes
4. Deploy to production
5. Monitor closely for first hour

### Post-Deployment

1. Verify health checks
2. Check error rates
3. Monitor metrics
4. Review logs

---

## 📚 Useful Queries

### Find Recent Errors
```sql
SELECT * FROM audit_logs 
WHERE error_message IS NOT NULL 
  AND timestamp > NOW() - INTERVAL '1 hour'
ORDER BY timestamp DESC;
```

### Check User Transaction Limits
```sql
SELECT * FROM user_transaction_limits 
WHERE user_id = 'user123' 
  AND transaction_date = CURRENT_DATE;
```

### Find Duplicate Transactions
```sql
SELECT idempotency_key, COUNT(*) as count
FROM transaction_idempotency
GROUP BY idempotency_key
HAVING COUNT(*) > 1;
```

### Account Balance Verification
```sql
SELECT a.id, a.name, a.balance_amount,
       COALESCE(SUM(CASE WHEN tl.type = 'DEBIT' THEN -tl.amount_value 
                         ELSE tl.amount_value END), 0) as calculated_balance
FROM accounts a
LEFT JOIN transaction_lines tl ON tl.account_id = a.id
GROUP BY a.id, a.name, a.balance_amount
HAVING a.balance_amount != COALESCE(SUM(...), 0);
```



