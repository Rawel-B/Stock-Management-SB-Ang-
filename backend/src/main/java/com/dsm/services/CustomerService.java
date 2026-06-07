package com.dsm.services;

import com.dsm.dto.request.RequestDTO.CustomerRequest;
import com.dsm.dto.response.ResponseDTO.CustomerResponse;
import com.dsm.entities.*;
import com.dsm.exception.*;
import com.dsm.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    //#region Main
    public CustomerResponse addCustomer(CustomerRequest request) {
        if (customerRepository.checkEmailValidity(request.getEmail())) {
            throw new DuplicateResourceException("a Customer With This Email Already Exists.");
        }

        Customer client = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return toResponse(customerRepository.save(client));
    }
    public CustomerResponse getCustomerById(String id) {
        return toResponse(findById(id));
    }
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    public Page<CustomerResponse> getCustomersAllPaged(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }
    public List<CustomerResponse> getCustomerByName(String name) {
        return customerRepository.getCustomerByName(name).stream().map(this::toResponse).collect(Collectors.toList());
    }
    public CustomerResponse updateCustomer(String id, CustomerRequest request) {
        Customer client = findById(id);

        if (!client.getEmail().equals(request.getEmail()) && customerRepository.checkEmailValidity(request.getEmail())) {
            throw new DuplicateResourceException("a Customer With This Email Already Exists.");
        }

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());
        client.setPhone(request.getPhone());
        return toResponse(customerRepository.save(client));
    }
    public void deleteCustomer(String id) {
        findById(id);
        customerRepository.deleteById(id);
    }
    public Customer findById(String id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer With " + id + " Was Not Found."));
    }
    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .ordersCount(orderRepository.getOrdersByCustomerId(customer.getId()).size())
                .createdAt(customer.getCreatedAt())
                .build();
    }
    //#endregion Main
    
}
