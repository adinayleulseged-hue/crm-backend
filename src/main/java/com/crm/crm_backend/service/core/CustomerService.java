package com.crm.crm_backend.service.core;

import com.crm.crm_backend.common.BaseService;
import com.crm.crm_backend.dto.request.CustomerCreateDTO;
import com.crm.crm_backend.dto.request.CustomerProfileUpdateDTO;
import com.crm.crm_backend.dto.request.CustomerUpdateDTO;
import com.crm.crm_backend.dto.response.CustomerResponseDTO;
import com.crm.crm_backend.exception.CustomerNotFoundException;
import com.crm.crm_backend.exception.DuplicateEntityException;
import com.crm.crm_backend.mapper.CustomerMapper;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.TerritoryRepository;
import com.crm.crm_backend.spec.CustomerSpecification;
import com.crm.crm_backend.util.generator.CustomerNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService extends BaseService<Customer, Long> {

    private final CustomerRepository customerRepository;
    private final com.crm.crm_backend.repository.UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;
    private final CustomerNumberGenerator customerNumberGenerator;
    private final com.crm.crm_backend.validator.CustomerValidator customerValidator;
    private final TerritoryRepository territoryRepository;

    @Override
    protected CustomerRepository getRepository() {
        return customerRepository;
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerCreateDTO dto) {
        log.info("Creating new customer with email: {}", dto.getEmail());

        customerValidator.validateCreate(dto);

        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEntityException("Customer with email " + dto.getEmail() + " already exists");
        }

        Customer customer = customerMapper.toEntity(dto);
        customer.setCustomerNumber(customerNumberGenerator.generate());
        applyTerritory(customer, dto.getTerritoryId());
        customerValidator.validateAmounts(customer);

        Customer saved = customerRepository.save(customer);
        log.info("Customer created with ID: {}", saved.getId());

        return customerMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        return customerMapper.toResponseDTO(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> getAllCustomers(Pageable pageable) {
        return searchCustomers(null, null, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> searchCustomers(
            String q,
            com.crm.crm_backend.model.enums.CustomerStatus status,
            com.crm.crm_backend.model.enums.CustomerType customerType,
            Long territoryId,
            String city,
            String country,
            LocalDate createdFrom,
            LocalDate createdTo,
            Pageable pageable) {

        Specification<Customer> spec = CustomerSpecification.withFilters(
                q, status, customerType, territoryId, city, country, createdFrom, createdTo);
        return customerRepository.findAll(spec, pageable).map(customerMapper::toResponseDTO);
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerUpdateDTO dto) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        customerValidator.validateUpdate(customer, dto);

        if (!customer.getEmail().equals(dto.getEmail()) &&
                customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEntityException("Email " + dto.getEmail() + " already in use");
        }

        customerMapper.updateEntity(dto, customer);
        if (dto.getTerritoryId() != null) {
            applyTerritory(customer, dto.getTerritoryId());
        }
        customerValidator.validateAmounts(customer);

        Customer updated = customerRepository.save(customer);
        log.info("Customer updated with ID: {}", updated.getId());
        return customerMapper.toResponseDTO(updated);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        customer.setDeleted(true);
        customerRepository.save(customer);

        log.info("Customer soft-deleted with ID: {}", id);
    }

    @Transactional
    public CustomerResponseDTO getProfile(String usernameOrEmail) {
        log.info("Fetching profile for user/customer: {}", usernameOrEmail);
        
        // 1. Try finding User by username or email
        com.crm.crm_backend.model.entity.User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElse(null);

        String emailToSearch = user != null ? user.getEmail() : usernameOrEmail;

        // 2. Find Customer by email or fallback to customer id if user exists
        Customer customer = customerRepository.findByEmail(emailToSearch)
                .or(() -> user != null ? customerRepository.findByIdAndDeletedFalse(user.getId()) : java.util.Optional.empty())
                .orElse(null);

        if (customer != null) {
            return customerMapper.toResponseDTO(customer);
        }

        // 3. If customer record does not exist yet for this user, auto-provision one
        Customer newCustomer = new Customer();
        newCustomer.setCustomerNumber(customerNumberGenerator.generate());
        newCustomer.setEmail(emailToSearch);
        newCustomer.setFirstName(user != null ? user.getUsername() : usernameOrEmail);
        newCustomer.setCompanyName((user != null ? user.getUsername() : usernameOrEmail) + " Organization");
        newCustomer.setStatus(com.crm.crm_backend.model.enums.CustomerStatus.ACTIVE);
        Customer saved = customerRepository.save(newCustomer);
        log.info("Auto-provisioned customer entity with ID: {} for user: {}", saved.getId(), usernameOrEmail);
        return customerMapper.toResponseDTO(saved);
    }

    @Transactional
    public CustomerResponseDTO updateProfile(String usernameOrEmail, CustomerProfileUpdateDTO dto) {
        log.info("Updating profile for user/customer: {}", usernameOrEmail);

        com.crm.crm_backend.model.entity.User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElse(null);

        String emailToSearch = user != null ? user.getEmail() : usernameOrEmail;

        Customer customer = customerRepository.findByEmail(emailToSearch)
                .or(() -> user != null ? customerRepository.findByIdAndDeletedFalse(user.getId()) : java.util.Optional.empty())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomerNumber(customerNumberGenerator.generate());
                    newCustomer.setEmail(emailToSearch);
                    newCustomer.setStatus(com.crm.crm_backend.model.enums.CustomerStatus.ACTIVE);
                    return newCustomer;
                });

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            customer.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            customer.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateEntityException("Email " + dto.getEmail() + " already in use");
            }
            customer.setEmail(dto.getEmail());
            if (user != null) {
                user.setEmail(dto.getEmail());
                userRepository.save(user);
            }
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(dto.getPassword());
            customer.setPassword(encoded);
            if (user != null) {
                user.setPassword(encoded);
                userRepository.save(user);
            }
        }

        Customer updated = customerRepository.save(customer);
        log.info("Profile updated for customer ID: {}", updated.getId());
        return customerMapper.toResponseDTO(updated);
    }

    // New method to update the authenticated user's profile
    @Transactional
    public com.crm.crm_backend.dto.response.CustomerResponseDTO updateProfile(Long userId, com.crm.crm_backend.dto.request.CustomerProfileUpdateDTO dto) {
        log.info("Updating profile for customer ID: {}", userId);
        Customer customer = customerRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + userId));

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            customer.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            customer.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !customer.getEmail().equals(dto.getEmail())) {
            if (customerRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateEntityException("Email " + dto.getEmail() + " already in use");
            }
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(dto.getPassword());
            customer.setPassword(encoded);
        }

        Customer updated = customerRepository.save(customer);
        log.info("Profile updated for customer ID: {}", updated.getId());
        return customerMapper.toResponseDTO(updated);
    }

    private void applyTerritory(Customer customer, Long territoryId) {
        if (territoryId == null) {
            return;
        }
        customer.setTerritory(territoryRepository.findById(territoryId)
                .orElseThrow(() -> new IllegalArgumentException("Territory not found: " + territoryId)));
    }
}
