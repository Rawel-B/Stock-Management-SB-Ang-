package com.dsm.services;

import com.dsm.dto.request.RequestDTO.CarrierRequest;
import com.dsm.dto.response.ResponseDTO.CarrierResponse;
import com.dsm.entities.*;
import com.dsm.exception.*;
import com.dsm.repositories.CarrierRepository;
import com.dsm.repositories.ShippingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CarrierService {
    private final CarrierRepository carrierRepository;
    private final ShippingRepository shippingRepository;

    //#region Main
    public CarrierResponse addCarrier(CarrierRequest request) {
        Carrier carrier = Carrier.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .rating(request.getRating() != null ? request.getRating() : BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(carrierRepository.save(carrier));
    }
    @Transactional(readOnly = true)
    public CarrierResponse getById(String id) {
        return toResponse(findById(id));
    }
    @Transactional(readOnly = true)
    public List<CarrierResponse> getAllCarriers() {
        return carrierRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<CarrierResponse> getActiveCarriers() {
        return carrierRepository.getAllActiveCarriers().stream().map(this::toResponse).collect(Collectors.toList());
    }
    public CarrierResponse updateCarrier(String id, CarrierRequest request) {
        Carrier carrier = findById(id);
        carrier.setName(request.getName());
        carrier.setPhone(request.getPhone());
        if (request.getRating() != null) carrier.setRating(request.getRating());
        if (request.getIsActive() != null) carrier.setIsActive(request.getIsActive());
        return toResponse(carrierRepository.save(carrier));
    }
    public void deleteCarrier(String id) {
        findById(id);
        carrierRepository.deleteById(id);
    }
    public Carrier findById(String id) {
        return carrierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Carrier Found With ID " + id));
    }
    private CarrierResponse toResponse(Carrier t) {
        return CarrierResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .phone(t.getPhone())
                .rating(t.getRating())
                .isActive(t.getIsActive())
                .shippingsCount(shippingRepository.getShippingsByCarrierId(t.getId()).size())
                .createdAt(t.getCreatedAt())
                .build();
    }
    //#endregion Main

}
