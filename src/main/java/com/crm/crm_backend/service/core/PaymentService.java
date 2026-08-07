package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.PaymentCreateDTO;
import com.crm.crm_backend.dto.request.PaymentUpdateDTO;
import com.crm.crm_backend.dto.response.PaymentResponseDTO;
import com.crm.crm_backend.event.publisher.PaymentEventPublisher;
import com.crm.crm_backend.mapper.PaymentMapper;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.entity.Invoice;
import com.crm.crm_backend.model.entity.Payment;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import com.crm.crm_backend.model.enums.PaymentStatus;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.InvoiceRepository;
import com.crm.crm_backend.repository.PaymentRepository;
import com.crm.crm_backend.service.workflow.DomainStatusGuard;
import com.crm.crm_backend.validator.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentValidator paymentValidator;

    public PaymentResponseDTO createPayment(PaymentCreateDTO dto) {

        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        paymentValidator.validatePaymentAmount(invoice, dto.getAmount());

        Payment payment = paymentMapper.toEntity(dto);

        payment.setInvoice(invoice);
        payment.setCustomer(customer);
        payment.setPaymentNumber("PAY-" + System.currentTimeMillis());
        payment.setStatus(PaymentStatus.COMPLETED);

        Payment saved = paymentRepository.save(payment);

        BigDecimal paidAmount = (invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO)
                .add(payment.getAmount());

        invoice.setPaidAmount(paidAmount);

        BigDecimal balance = invoice.getTotalAmount().subtract(paidAmount);

        invoice.setBalanceAmount(balance);

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            DomainStatusGuard.assertInvoiceTransition(invoice.getStatus(), InvoiceStatus.PAID);
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (invoice.getStatus() == InvoiceStatus.DRAFT
                || invoice.getStatus() == InvoiceStatus.SENT) {
            DomainStatusGuard.assertInvoiceTransition(invoice.getStatus(), InvoiceStatus.PENDING);
            invoice.setStatus(InvoiceStatus.PENDING);
        }

        invoiceRepository.save(invoice);
        paymentEventPublisher.publishPaymentReceived(saved);

        return paymentMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return paymentMapper.toResponseDTO(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getAllPayments(Pageable pageable) {

        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toResponseDTO);
    }

    public PaymentResponseDTO updatePayment(Long id,
                                            PaymentUpdateDTO dto) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        paymentMapper.updateEntityFromDTO(dto, payment);

        Payment updated = paymentRepository.save(payment);

        return paymentMapper.toResponseDTO(updated);
    }

    public void deletePayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        paymentRepository.delete(payment);

        log.info("Payment {} deleted", id);
    }
}
