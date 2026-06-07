package com.dsm.services;

import com.dsm.dto.request.RequestDTO.SupplierRequest;
import com.dsm.dto.response.ResponseDTO.SupplierResponse;
import com.dsm.entities.Supplier;
import com.dsm.exception.DuplicateResourceException;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.OrderRepository;
import com.dsm.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final OrderRepository orderRepository;

    public SupplierResponse addSupplier(SupplierRequest request) {
        if (request.getEmail() != null && supplierRepository.checkEmailValidity(request.getEmail())) {
            throw new DuplicateResourceException("a Supplier With This Email Already Exists.");
        }

        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(supplierRepository.save(supplier));
    }
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(String id) {
        return toResponse(findById(id));
    }
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<SupplierResponse> getActiveSuppliers() {
        return supplierRepository.getAllActiveSuppliers().stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<SupplierResponse> getSupplierByName(String name) {
        return supplierRepository.getSupplierByName(name).stream().map(this::toResponse).collect(Collectors.toList());
    }
    public SupplierResponse updateSupplier(String id, SupplierRequest request) {
        Supplier supplier = findById(id);

        if (request.getEmail() != null && supplier.getEmail() != null && !supplier.getEmail().equals(request.getEmail()) && supplierRepository.checkEmailValidity(request.getEmail())) {
            throw new DuplicateResourceException("a Supplier With This Email Already Exists.");
        }

        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        if (request.getIsActive() != null) supplier.setIsActive(request.getIsActive());
        return toResponse(supplierRepository.save(supplier));
    }
    public void deleteSupplier(String id) {
        findById(id);
        supplierRepository.deleteById(id);
    }
    public Supplier findById(String id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier With " + id + " Was Not Found."));
    }
    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .isActive(supplier.getIsActive())
                .ordersCount(orderRepository.getOrdersBySupplierId(supplier.getId()).size())
                .createdAt(supplier.getCreatedAt())
                .build();
    }

}
