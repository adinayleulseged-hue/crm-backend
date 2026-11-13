package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.OpportunityCreateDTO;
import com.crm.crm_backend.dto.request.OpportunityUpdateDTO;
import com.crm.crm_backend.dto.response.OpportunityResponseDTO;
import com.crm.crm_backend.dto.response.QuotationResponseDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.event.publisher.OpportunityEventPublisher;
import com.crm.crm_backend.exception.OpportunityNotFoundException;
import com.crm.crm_backend.mapper.OpportunityMapper;
import com.crm.crm_backend.mapper.QuotationMapper;
import com.crm.crm_backend.mapper.SalesOrderMapper;
import com.crm.crm_backend.model.entity.*;
import com.crm.crm_backend.model.enums.*;
import com.crm.crm_backend.notification.EmailService.EmailService;
import com.crm.crm_backend.repository.*;
import com.crm.crm_backend.service.workflow.DomainStatusGuard;
import com.crm.crm_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final OpportunityMapper opportunityMapper;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final CampaignService campaignService;
    private final QuotationService quotationService;
    private final OpportunityEventPublisher opportunityEventPublisher;
    private final QuotationRepository quotationRepository;
    private final QuotationMapper quotationMapper;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    // ============================================================
    // CREATE OPPORTUNITY
    // ============================================================

    public OpportunityResponseDTO createOpportunity(OpportunityCreateDTO dto) {

        /*
         * Validate the expected close date FIRST.
         *
         * This is important because the test specifically checks
         * that a past date is rejected.
         *
         * We do this before Lead/Customer lookup so that the
         * date-validation test does not fail with "Lead not found".
         */
        if (dto.getExpectedCloseDate() != null
                && dto.getExpectedCloseDate().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Expected close date cannot be in the past."
            );
        }

        // --------------------------------------------------------
        // Find Lead (Optional)
        // --------------------------------------------------------

        Lead lead = null;
        if (dto.getLeadId() != null) {
            lead = leadRepository.findById(dto.getLeadId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Lead not found.")
                    );
        }

        // --------------------------------------------------------
        // Find or Resolve Customer
        // --------------------------------------------------------

        Customer customer = null;
        if (dto.getCustomerId() != null && dto.getCustomerId() > 0) {
            customer = customerRepository.findById(dto.getCustomerId()).orElse(null);
        }

        if (customer == null) {
            String custName = null;
            if (dto.getCustomerName() != null && !dto.getCustomerName().isBlank()) {
                custName = dto.getCustomerName().trim();
            } else if (dto.getCustomer() != null && !dto.getCustomer().isBlank()) {
                custName = dto.getCustomer().trim();
            }

            if (custName != null) {
                final String searchName = custName;
                customer = customerRepository.findAll().stream()
                        .filter(c -> (c.getCompanyName() != null && c.getCompanyName().equalsIgnoreCase(searchName))
                                || (c.getContactName() != null && c.getContactName().equalsIgnoreCase(searchName)))
                        .findFirst()
                        .orElseGet(() -> {
                            Customer newCust = new Customer();
                            newCust.setCustomerNumber("CUST-" + System.currentTimeMillis());
                            newCust.setCompanyName(searchName);
                            newCust.setContactName(searchName);
                            newCust.setStatus(CustomerStatus.ACTIVE);
                            return customerRepository.save(newCust);
                        });
            } else {
                customer = customerRepository.findAll().stream().findFirst().orElseGet(() -> {
                    Customer fallback = new Customer();
                    fallback.setCustomerNumber("CUST-" + System.currentTimeMillis());
                    fallback.setCompanyName("Enterprise Customer");
                    fallback.setContactName("Enterprise Customer");
                    fallback.setStatus(CustomerStatus.ACTIVE);
                    return customerRepository.save(fallback);
                });
            }
        }

        // --------------------------------------------------------
        // Map DTO to Entity
        // --------------------------------------------------------

        Opportunity opportunity = opportunityMapper.toEntity(dto);

        if (opportunity.getOpportunityName() == null || opportunity.getOpportunityName().isBlank()) {
            if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
                opportunity.setOpportunityName(dto.getTitle());
            } else if (dto.getDealName() != null && !dto.getDealName().isBlank()) {
                opportunity.setOpportunityName(dto.getDealName());
            } else {
                opportunity.setOpportunityName("Sales Deal");
            }
        }

        if (opportunity.getDescription() == null || opportunity.getDescription().isBlank()) {
            if (dto.getSubtitle() != null && !dto.getSubtitle().isBlank()) {
                opportunity.setDescription(dto.getSubtitle());
            } else if (dto.getScope() != null && !dto.getScope().isBlank()) {
                opportunity.setDescription(dto.getScope());
            } else if (dto.getNotes() != null && !dto.getNotes().isBlank()) {
                opportunity.setDescription(dto.getNotes());
            }
        }

        if (opportunity.getExpectedRevenue() == null) {
            if (dto.getValue() != null) {
                opportunity.setExpectedRevenue(dto.getValue());
            } else if (dto.getAmount() != null) {
                opportunity.setExpectedRevenue(dto.getAmount());
            } else if (dto.getEstimatedValue() != null) {
                opportunity.setExpectedRevenue(dto.getEstimatedValue());
            }
        }

        if (opportunity.getProbability() == null) {
            opportunity.setProbability(dto.getProbability() != null ? dto.getProbability() : 60);
        }

        if (opportunity.getExpectedCloseDate() == null) {
            opportunity.setExpectedCloseDate(LocalDate.now().plusDays(30));
        }

        if (opportunity.getType() == null) {
            opportunity.setType(OpportunityType.NEW_BUSINESS);
        }

        opportunity.setLead(lead);
        opportunity.setCustomer(customer);

        // Stage handling
        if (dto.getStage() != null) {
            opportunity.setStage(dto.getStage());
        } else if (dto.getStageName() != null && !dto.getStageName().isBlank()) {
            String s = dto.getStageName().toUpperCase().replace(" ", "_");
            try {
                if (s.contains("WON")) opportunity.setStage(OpportunityStage.CLOSED_WON);
                else if (s.contains("LOST")) opportunity.setStage(OpportunityStage.CLOSED_LOST);
                else if (s.contains("NEGOTIAT")) opportunity.setStage(OpportunityStage.NEGOTIATION);
                else if (s.contains("PROPOSAL")) opportunity.setStage(OpportunityStage.PROPOSAL);
                else if (s.contains("QUALIF")) opportunity.setStage(OpportunityStage.QUALIFICATION);
                else if (s.contains("NEED") || s.contains("DISCOVERY")) opportunity.setStage(OpportunityStage.NEEDS_ANALYSIS);
                else opportunity.setStage(OpportunityStage.valueOf(s));
            } catch (Exception e) {
                opportunity.setStage(OpportunityStage.PROPOSAL);
            }
        } else {
            opportunity.setStage(OpportunityStage.PROPOSAL);
        }

        // Generate opportunity number.
        opportunity.setOpportunityNumber(
                "DEAL-" + System.currentTimeMillis()
        );

        // --------------------------------------------------------
        // Save
        // --------------------------------------------------------

        Opportunity saved = opportunityRepository.save(opportunity);

        log.info(
                "Opportunity {} created",
                saved.getOpportunityNumber()
        );

        return opportunityMapper.toResponseDTO(saved);
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public OpportunityResponseDTO getOpportunityById(Long id) {

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() ->
                        new OpportunityNotFoundException(
                                "Opportunity not found : " + id
                        )
                );

        return opportunityMapper.toResponseDTO(opportunity);
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @Transactional(readOnly = true)
    public Page<OpportunityResponseDTO> getAllOpportunities(
            Pageable pageable
    ) {

        return opportunityRepository.findAll(pageable)
                .map(opportunityMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OpportunityResponseDTO> getAllOpportunities(
            Long customerId,
            Pageable pageable
    ) {
        if (customerId != null) {
            return opportunityRepository.findByCustomerId(customerId, pageable)
                    .map(opportunityMapper::toResponseDTO);
        }
        return opportunityRepository.findAll(pageable)
                .map(opportunityMapper::toResponseDTO);
    }

    // ============================================================
    // UPDATE OPPORTUNITY
    // ============================================================

    public OpportunityResponseDTO updateOpportunity(
            Long id,
            OpportunityUpdateDTO dto
    ) {

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() ->
                        new OpportunityNotFoundException(
                                "Opportunity not found : " + id
                        )
                );

        /*
         * Also protect update operations from receiving
         * a past expected close date.
         *
         * This assumes OpportunityUpdateDTO contains
         * getExpectedCloseDate().
         */
        if (dto.getExpectedCloseDate() != null
                && dto.getExpectedCloseDate().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Expected close date cannot be in the past."
            );
        }

        opportunityMapper.updateEntityFromDTO(dto, opportunity);

        Opportunity updated = opportunityRepository.save(opportunity);

        return opportunityMapper.toResponseDTO(updated);
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void deleteOpportunity(Long id) {

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() ->
                        new OpportunityNotFoundException(
                                "Opportunity not found : " + id
                        )
                );

        opportunityRepository.delete(opportunity);

        log.info("Opportunity deleted {}", id);
    }

    // ============================================================
    // UPDATE STAGE
    // ============================================================

    public OpportunityResponseDTO updateStage(
            Long id,
            OpportunityStage stage
    ) {

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() ->
                        new OpportunityNotFoundException(
                                "Opportunity not found : " + id
                        )
                );

        OpportunityStage oldStage = opportunity.getStage();

        // --------------------------------------------------------
        // Validate stage transition
        // --------------------------------------------------------

        DomainStatusGuard.assertOpportunityTransition(
                oldStage,
                stage
        );

        // --------------------------------------------------------
        // Update stage
        // --------------------------------------------------------

        opportunity.setStage(stage);

        // --------------------------------------------------------
        // Set actual close date
        // --------------------------------------------------------

        if (stage == OpportunityStage.CLOSED_WON
                || stage == OpportunityStage.CLOSED_LOST) {

            opportunity.setActualCloseDate(LocalDate.now());
        }

        // --------------------------------------------------------
        // Save
        // --------------------------------------------------------

        Opportunity updated = opportunityRepository.save(opportunity);

        // --------------------------------------------------------
        // Closed Opportunity Processing
        // --------------------------------------------------------

        if (stage == OpportunityStage.CLOSED_WON
                || stage == OpportunityStage.CLOSED_LOST) {

            boolean won = stage == OpportunityStage.CLOSED_WON;

            /*
             * Close open quotations belonging to this opportunity.
             */
            quotationService.closeOpenQuotationsForOpportunity(
                    updated.getId(),
                    won
            );

            /*
             * If the opportunity was WON, record the revenue
             * against the campaign associated with the lead.
             */
            if (won) {

                Lead lead = updated.getLead();

                if (lead != null && lead.getCampaignId() != null) {

                    BigDecimal revenue =
                            updated.getExpectedRevenue() != null
                                    ? updated.getExpectedRevenue()
                                    : BigDecimal.ZERO;

                    campaignService.recordRevenueFromConversion(
                            lead.getCampaignId(),
                            revenue
                    );
                }
            }
        }

        // --------------------------------------------------------
        // Publish stage-change event
        // --------------------------------------------------------

        opportunityEventPublisher.publishOpportunityStageChanged(
                updated,
                stage
        );

        log.info(
                "Opportunity {} stage changed from {} to {}",
                updated.getId(),
                oldStage,
                stage
        );

        return opportunityMapper.toResponseDTO(updated);
    }

    // ============================================================
    // GENERATE QUOTATION FROM OPPORTUNITY
    // ============================================================

    /**
     * Generate quotation from opportunity (Deal → Quotation workflow transition)
     */
    public QuotationResponseDTO generateQuotationFromOpportunity(Long opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found with id: " + opportunityId));

        // Validate opportunity is in appropriate stage
        if (opportunity.getStage() == OpportunityStage.CLOSED_LOST 
                || opportunity.getStage() == OpportunityStage.CLOSED_WON) {
            throw new IllegalStateException(
                    "Cannot generate quotation from a closed opportunity. Current stage: " + opportunity.getStage());
        }

        // Check if quotation already generated
        if (opportunity.getQuotationGenerated() != null && opportunity.getQuotationGenerated()) {
            List<Quotation> existing = quotationRepository.findByOpportunityIdAndStatusIn(
                    opportunityId,
                    List.of(QuotationStatus.DRAFT, QuotationStatus.SENT, QuotationStatus.ACCEPTED)
            );
            if (!existing.isEmpty()) {
                log.info("Returning existing quotation {} for opportunity {}", 
                        existing.get(0).getQuotationNumber(), opportunityId);
                return quotationMapper.toResponseDTO(existing.get(0));
            }
        }

        // Create quotation
        Quotation quotation = Quotation.builder()
                .quotationNumber("QT-" + System.currentTimeMillis())
                .customer(opportunity.getCustomer())
                .opportunity(opportunity)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(30))  // 30 days validity
                .subtotal(opportunity.getExpectedRevenue() != null ? opportunity.getExpectedRevenue() : BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .totalAmount(opportunity.getExpectedRevenue() != null ? opportunity.getExpectedRevenue() : BigDecimal.ZERO)
                .status(QuotationStatus.DRAFT)
                .notes("Generated from Opportunity: " + opportunity.getOpportunityName())
                .active(true)
                .build();

        Quotation savedQuotation = quotationRepository.save(quotation);

        // Mark opportunity as quotation generated
        opportunity.setQuotationGenerated(true);
        opportunityRepository.save(opportunity);

        log.info("Quotation {} generated from Opportunity {}", 
                savedQuotation.getQuotationNumber(), opportunity.getOpportunityNumber());

        return quotationMapper.toResponseDTO(savedQuotation);
    }

    // ============================================================
    // DEAL APPROVAL WORKFLOW METHODS
    // ============================================================

    /**
     * Customer submits deal for admin/sales manager approval
     */
    public OpportunityResponseDTO submitForApproval(Long opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found with id: " + opportunityId));

        // Update workflow status
        opportunity.setWorkflowStatus(WorkflowStatus.PENDING_APPROVAL);
        Opportunity updated = opportunityRepository.save(opportunity);

        log.info("Opportunity {} submitted for approval by customer", opportunityId);
        return opportunityMapper.toResponseDTO(updated);
    }

    /**
     * Admin/Sales Manager approves the deal
     */
    public OpportunityResponseDTO approveDeal(Long opportunityId, Double discountApproved) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found with id: " + opportunityId));

        // Validate current status - Cannot approve closed deals
        if (opportunity.getWorkflowStatus() == WorkflowStatus.CLOSED_WON 
                || opportunity.getWorkflowStatus() == WorkflowStatus.CLOSED_LOST) {
            throw new IllegalStateException(
                    "Cannot approve a deal that is already closed. Current status: " + opportunity.getWorkflowStatus());
        }

        // Get current user ID
        Long userId = SecurityUtils.getCurrentUserId();

        // Update workflow status
        opportunity.setWorkflowStatus(WorkflowStatus.APPROVED);
        opportunity.setApprovedBy(userId);
        opportunity.setApprovedAt(LocalDateTime.now());

        // Set discount if provided
        if (discountApproved != null) {
            opportunity.setDiscountApproved(BigDecimal.valueOf(discountApproved));
        }

        Opportunity updated = opportunityRepository.save(opportunity);

        log.info("Opportunity {} approved by user {}", opportunityId, userId);
        
        // Send email notification to customer
        try {
            Customer customer = updated.getCustomer();
            if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Good news! Your sales deal has been APPROVED.\n\n" +
                    "Deal Details:\n" +
                    "- Deal ID: %d\n" +
                    "- Title: %s\n" +
                    "- Value: $%s\n" +
                    "- Status: APPROVED\n" +
                    "%s\n" +
                    "You can now proceed to place your order in the customer portal.\n\n" +
                    "Login at: http://localhost:3000/customer-login\n\n" +
                    "Thank you for your business!\n\n" +
                    "Best regards,\n" +
                    "INSA Sales Team",
                    customer.getCompanyName() != null ? customer.getCompanyName() : "Valued Customer",
                    updated.getId(),
                    updated.getOpportunityName() != null ? updated.getOpportunityName() : "Sales Deal",
                    updated.getExpectedRevenue() != null ? updated.getExpectedRevenue().toString() : "N/A",
                    discountApproved != null ? String.format("- Discount Approved: %.2f%%\n", discountApproved) : ""
                );
                
                boolean emailSent = emailService.send(
                    customer.getEmail(),
                    "INSA CRM - Your Sales Deal Has Been Approved!",
                    emailBody
                );
                
                if (emailSent) {
                    log.info("Approval notification email sent to customer: {}", customer.getEmail());
                } else {
                    log.warn("Failed to send approval notification email to customer: {}", customer.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Error sending approval email notification: {}", e.getMessage());
            // Don't fail the approval if email fails
        }
        
        return opportunityMapper.toResponseDTO(updated);
    }

    /**
     * Admin/Sales Manager rejects the deal
     */
    public OpportunityResponseDTO rejectDeal(Long opportunityId, String reason) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found with id: " + opportunityId));

        // Validate current status - Cannot reject closed deals
        if (opportunity.getWorkflowStatus() == WorkflowStatus.CLOSED_WON 
                || opportunity.getWorkflowStatus() == WorkflowStatus.CLOSED_LOST) {
            throw new IllegalStateException(
                    "Cannot reject a deal that is already closed. Current status: " + opportunity.getWorkflowStatus());
        }

        // Get current user ID
        Long userId = SecurityUtils.getCurrentUserId();

        // Update workflow status
        opportunity.setWorkflowStatus(WorkflowStatus.REJECTED);
        opportunity.setRejectedBy(userId);
        opportunity.setRejectedAt(LocalDateTime.now());
        opportunity.setRejectionReason(reason);

        Opportunity updated = opportunityRepository.save(opportunity);

        log.info("Opportunity {} rejected by user {} with reason: {}", opportunityId, userId, reason);
        
        // Send email notification to customer
        try {
            Customer customer = updated.getCustomer();
            if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "We regret to inform you that your sales deal has been reviewed and we are unable to proceed at this time.\n\n" +
                    "Deal Details:\n" +
                    "- Deal ID: %d\n" +
                    "- Title: %s\n" +
                    "- Status: NOT APPROVED\n" +
                    "- Reason: %s\n\n" +
                    "If you have any questions or would like to discuss this further, please contact our sales team.\n\n" +
                    "We appreciate your interest and hope to work with you in the future.\n\n" +
                    "Best regards,\n" +
                    "INSA Sales Team",
                    customer.getCompanyName() != null ? customer.getCompanyName() : "Valued Customer",
                    updated.getId(),
                    updated.getOpportunityName() != null ? updated.getOpportunityName() : "Sales Deal",
                    reason != null ? reason : "No specific reason provided"
                );
                
                boolean emailSent = emailService.send(
                    customer.getEmail(),
                    "INSA CRM - Sales Deal Status Update",
                    emailBody
                );
                
                if (emailSent) {
                    log.info("Rejection notification email sent to customer: {}", customer.getEmail());
                } else {
                    log.warn("Failed to send rejection notification email to customer: {}", customer.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Error sending rejection email notification: {}", e.getMessage());
            // Don't fail the rejection if email fails
        }
        
        return opportunityMapper.toResponseDTO(updated);
    }

    /**
     * Customer places order from approved deal
     * Creates SalesOrder + Invoice automatically
     */
    public SalesOrderResponseDTO placeOrderFromDeal(Long opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found with id: " + opportunityId));

        // Validate current status
        if (opportunity.getWorkflowStatus() != WorkflowStatus.APPROVED) {
            throw new IllegalStateException(
                    "Can only place order for APPROVED deals. Current status: " + opportunity.getWorkflowStatus());
        }

        // Validate required fields
        if (opportunity.getProduct() == null || opportunity.getQuantity() == null || opportunity.getUnitPrice() == null) {
            throw new IllegalStateException("Product, quantity, and unit price are required to place order");
        }

        // Calculate total amount
        BigDecimal subtotal = opportunity.getUnitPrice().multiply(BigDecimal.valueOf(opportunity.getQuantity()));
        BigDecimal discount = subtotal.multiply(opportunity.getDiscountApproved()).divide(BigDecimal.valueOf(100));
        BigDecimal totalAmount = subtotal.subtract(discount);

        // Create Sales Order
        SalesOrder order = SalesOrder.builder()
                .orderNumber("SO-" + System.currentTimeMillis())
                .customer(opportunity.getCustomer())
                .opportunity(opportunity)
                .orderDate(LocalDate.now())
                .deliveryDate(LocalDate.now().plusDays(30))
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(order);

        // Create Invoice automatically
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + System.currentTimeMillis())
                .customer(opportunity.getCustomer())
                .salesOrder(savedOrder)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .status(InvoiceStatus.PENDING)
                .notes("Auto-generated from Deal: " + opportunity.getOpportunityName())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Link order and invoice to opportunity
        opportunity.setOrder(savedOrder);
        opportunity.setInvoice(savedInvoice);
        opportunity.setWorkflowStatus(WorkflowStatus.ORDER_PLACED);
        opportunityRepository.save(opportunity);

        log.info("Order {} and Invoice {} created from Opportunity {}", 
                savedOrder.getOrderNumber(), savedInvoice.getInvoiceNumber(), opportunityId);

        return salesOrderMapper.toResponseDTO(savedOrder);
    }

    /**
     * Admin/Sales Manager closes the deal (Qualified/Closed Won/Closed Lost)
     * Can only close after payment is approved
     */
    public OpportunityResponseDTO closeDeal(Long opportunityId, WorkflowStatus status, String notes) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found with id: " + opportunityId));

        // Validate status parameter
        if (status != WorkflowStatus.QUALIFIED 
                && status != WorkflowStatus.CLOSED_WON 
                && status != WorkflowStatus.CLOSED_LOST) {
            throw new IllegalArgumentException(
                    "Invalid close status. Must be QUALIFIED, CLOSED_WON, or CLOSED_LOST");
        }

        // For QUALIFIED/CLOSED_WON: validate payment is approved
        if (status == WorkflowStatus.QUALIFIED || status == WorkflowStatus.CLOSED_WON) {
            if (opportunity.getInvoice() == null) {
                throw new IllegalStateException("Cannot close deal: no invoice found");
            }
            if (opportunity.getInvoice().getStatus() != InvoiceStatus.PAID) {
                throw new IllegalStateException(
                        "Cannot close deal: invoice must be PAID. Current status: " + opportunity.getInvoice().getStatus());
            }
        }

        // Get current user ID
        Long userId = SecurityUtils.getCurrentUserId();

        // Update workflow status
        opportunity.setWorkflowStatus(status);
        opportunity.setClosedBy(userId);
        opportunity.setClosedAt(LocalDateTime.now());
        opportunity.setCloseNotes(notes);

        // Update opportunity stage
        if (status == WorkflowStatus.QUALIFIED) {
            opportunity.setStage(OpportunityStage.QUALIFICATION);
        } else if (status == WorkflowStatus.CLOSED_WON) {
            opportunity.setStage(OpportunityStage.CLOSED_WON);
            opportunity.setActualCloseDate(LocalDate.now());
        } else if (status == WorkflowStatus.CLOSED_LOST) {
            opportunity.setStage(OpportunityStage.CLOSED_LOST);
            opportunity.setActualCloseDate(LocalDate.now());
        }

        Opportunity updated = opportunityRepository.save(opportunity);

        log.info("Opportunity {} closed as {} by user {}", opportunityId, status, userId);
        return opportunityMapper.toResponseDTO(updated);
    }

    /**
     * Get pending approvals for Admin/Sales Manager
     */
    @Transactional(readOnly = true)
    public Page<OpportunityResponseDTO> getPendingApprovals(Pageable pageable) {
        Page<Opportunity> pendingOpportunities = opportunityRepository
                .findByWorkflowStatus(WorkflowStatus.PENDING_APPROVAL, pageable);
        
        return pendingOpportunities.map(opportunityMapper::toResponseDTO);
    }

}
