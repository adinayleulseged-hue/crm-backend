package com.crm.crm_backend.aspect;

import com.crm.crm_backend.model.entity.AuditTrail;
import com.crm.crm_backend.model.enums.AuditAction;
import com.crm.crm_backend.model.enums.AuditModule;
import com.crm.crm_backend.service.core.AuditTrailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditTrailService auditTrailService;

    @AfterReturning(
            "execution(* com.crm.crm_backend.service.*.*.create*(..)) || " +
                    "execution(* com.crm.crm_backend.service.*.*.save*(..))")
    public void auditCreate(JoinPoint joinPoint) {

        saveAudit(joinPoint, AuditAction.CREATE);

    }

    @AfterReturning(
            "execution(* com.crm.crm_backend.service.*.*.update*(..))")
    public void auditUpdate(JoinPoint joinPoint) {

        saveAudit(joinPoint, AuditAction.UPDATE);

    }

    @AfterReturning(
            "execution(* com.crm.crm_backend.service.*.*.delete*(..))")
    public void auditDelete(JoinPoint joinPoint) {

        saveAudit(joinPoint, AuditAction.DELETE);

    }

    private void saveAudit(JoinPoint joinPoint, AuditAction action) {

        String serviceName = joinPoint.getTarget().getClass().getSimpleName();

        AuditTrail auditTrail = AuditTrail.builder()
                .module(resolveModule(serviceName))
                .action(action)
                .entityId(0L)
                .entityName(serviceName.replace("Service", ""))
                .performedBy("SYSTEM")
                .performedAt(LocalDateTime.now())
                .description(action + " executed on " + serviceName)
                .ipAddress("127.0.0.1")
                .build();

        auditTrailService.save(auditTrail);

        log.info("Audit saved for {}", serviceName);
    }

    private AuditModule resolveModule(String serviceName) {

        if (serviceName.contains("Customer"))
            return AuditModule.CUSTOMER;

        if (serviceName.contains("Lead"))
            return AuditModule.LEAD;

        if (serviceName.contains("Opportunity"))
            return AuditModule.OPPORTUNITY;

        if (serviceName.contains("Interaction"))
            return AuditModule.INTERACTION;

        if (serviceName.contains("Campaign"))
            return AuditModule.CAMPAIGN;

        if (serviceName.contains("SalesOrder"))
            return AuditModule.SALES_ORDER;

        if (serviceName.contains("Invoice"))
            return AuditModule.INVOICE;

        if (serviceName.contains("Payment"))
            return AuditModule.PAYMENT;

        return AuditModule.DASHBOARD;
    }

}
