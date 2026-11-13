package com.crm.crm_backend.repository;


import com.crm.crm_backend.model.entity.Opportunity;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.model.enums.WorkflowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    long count();

    long countByStage(OpportunityStage stage);
    Optional<Opportunity> findByOpportunityNumber(String opportunityNumber);

    boolean existsByOpportunityNumber(String opportunityNumber);

    List<Opportunity> findByStage(OpportunityStage stage);

    List<Opportunity> findByAssignedTo(String assignedTo);

    // Workflow status queries
    Page<Opportunity> findByWorkflowStatus(WorkflowStatus workflowStatus, Pageable pageable);

    Page<Opportunity> findByCustomerId(Long customerId, Pageable pageable);

    List<Opportunity> findByCustomerId(Long customerId);
}
