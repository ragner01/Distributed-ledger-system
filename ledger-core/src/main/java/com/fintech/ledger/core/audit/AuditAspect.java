package com.fintech.ledger.core.audit;

import com.fintech.common.audit.Auditable;
import com.fintech.ledger.core.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        String traceId = UUID.randomUUID().toString();

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logAudit(joinPoint, auditable, result, error, duration, traceId);
        }
    }

    private void logAudit(ProceedingJoinPoint joinPoint, Auditable auditable, Object result, Throwable error,
            long duration, String traceId) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String action = auditable.action().isEmpty() ? methodName : auditable.action();

        String user = "ANONYMOUS";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            user = authentication.getName();
        }

        String ipAddress = "UNKNOWN";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getRemoteAddr();
        }

        // Log to console
        log.info(
                "AUDIT LOG -> Action: [{}], User: [{}], IP: [{}], Args: [{}], Result: [{}], Error: [{}], Duration: [{}ms], TraceId: [{}]",
                action, user, ipAddress, Arrays.toString(args), result, error != null ? error.getMessage() : "SUCCESS",
                duration, traceId);

        // Persist to database
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setUserName(user);
            auditLog.setIpAddress(ipAddress);
            auditLog.setMethodName(methodName);
            auditLog.setArguments(truncateString(Arrays.toString(args), 5000));
            auditLog.setResult(result != null ? truncateString(result.toString(), 5000) : null);
            auditLog.setErrorMessage(error != null ? truncateString(error.getMessage(), 5000) : null);
            auditLog.setDurationMs(duration);
            auditLog.setTraceId(traceId);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Don't fail the transaction if audit logging fails
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
