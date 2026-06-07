package com.dsm.services;

import com.dsm.dto.request.*;
import com.dsm.dto.request.RequestDTO.ShippingRequest;
import com.dsm.dto.response.*;
import com.dsm.dto.response.ResponseDTO.CarrierResponse;
import com.dsm.dto.response.ResponseDTO.ShippingResponse;
import com.dsm.entities.*;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.CarrierRepository;
import com.dsm.repositories.OrderRepository;
import com.dsm.repositories.ShippingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingService {
    private final ShippingRepository shippingRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CarrierRepository carrierRepository;
    private final StockService stockService;

    public ShippingResponse addShipping(ShippingRequest request) {
        Order order = orderService.findById(request.getOrderId());
        Carrier carrier = null;

        if (request.getCarrierId() != null) {
            carrier = carrierRepository.findById(request.getCarrierId()).orElseThrow(() -> new ResourceNotFoundException("Carrier Was Not Found."));
        }

        Shipping shipping = Shipping.builder()
                .orderId(order.getId())
                .carrierId(carrier != null ? carrier.getId() : null)
                .deliveryDate(request.getDeliveryDate())
                .cost(request.getCost())
                .shippingAddress(request.getShippingAddress())
                .trackingNumber(request.getTrackingNumber())
                .remark(request.getRemark())
                .status(Shipping.ShippingStatus.inPerparation)
                .build();

        if (order.getStatus() == Order.OrderStatus.validated) {
            order.setStatus(Order.OrderStatus.ongoing);
            orderRepository.save(order);
        }

        return toResponse(shippingRepository.save(shipping));
    }
    public ShippingResponse getShippingById(String id) {
        return toResponse(findShippingById(id));
    }
    public List<ShippingResponse> getAllShippings() {
        return shippingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public List<ShippingResponse> getShippingsByOrder(String orderId) {
        return shippingRepository.getShippingsByOrderId(orderId).stream().map(this::toResponse).collect(Collectors.toList());
    }
    public List<ShippingResponse> getShippingsByStatus(Shipping.ShippingStatus status) {
        return shippingRepository.getShippingsByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }
    public ShippingResponse updateShippingStatus(String id, Shipping.ShippingStatus status) {
        Shipping shipping = findShippingById(id);
        boolean shouldReceiveStock = shipping.getStatus() != Shipping.ShippingStatus.delivered && status == Shipping.ShippingStatus.delivered;
        shipping.setStatus(status);

        if (status == Shipping.ShippingStatus.delivered) {
            shipping.setReceiptDate(LocalDateTime.now());
            Order order = orderService.findById(shipping.getOrderId());
            order.setStatus(Order.OrderStatus.delivered);
            orderRepository.save(order);
            if (shouldReceiveStock) {
                stockService.receiveOrder(order);
            }
        }

        return toResponse(shippingRepository.save(shipping));
    }
    public ShippingResponse updateShipping(String id, ShippingRequest request) {
        Shipping shipping = findShippingById(id);

        if (request.getCarrierId() != null) {
            Carrier carrier = carrierRepository.findById(request.getCarrierId()).orElseThrow(() -> new ResourceNotFoundException("Carrier Was Not Found."));
            shipping.setCarrierId(carrier.getId());
        }

        shipping.setDeliveryDate(request.getDeliveryDate());
        shipping.setCost(request.getCost());
        shipping.setShippingAddress(request.getShippingAddress());
        shipping.setTrackingNumber(request.getTrackingNumber());
        shipping.setRemark(request.getRemark());
        return toResponse(shippingRepository.save(shipping));
    }
    public void deleteShipping(String id) {
        findShippingById(id);
        shippingRepository.deleteById(id);
    }
    private Shipping findShippingById(String id) {
        return shippingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipping With ID " + id + " Was Not Found."));
    }
    private ShippingResponse toResponse(Shipping shipping) {
        CarrierResponse carrierResponse = null;
        Order order = null;

        if (shipping.getCarrierId() != null) {
            Carrier carrier = carrierRepository.findById(shipping.getCarrierId()).orElse(null);

            if (carrier != null) {
                carrierResponse = CarrierResponse.builder()
                        .id(carrier.getId())
                        .name(carrier.getName())
                        .phone(carrier.getPhone())
                        .rating(carrier.getRating())
                        .isActive(carrier.getIsActive())
                        .createdAt(carrier.getCreatedAt())
                        .build();
            }
        }
        if (shipping.getOrderId() != null) {
            order = orderService.findById(shipping.getOrderId());
        }

        return ShippingResponse.builder()
                .id(shipping.getId())
                .orderId(shipping.getOrderId())
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .carrier(carrierResponse)
                .deliveryDate(shipping.getDeliveryDate())
                .receiptDate(shipping.getReceiptDate())
                .cost(shipping.getCost())
                .status(shipping.getStatus())
                .shippingAddress(shipping.getShippingAddress())
                .trackingNumber(shipping.getTrackingNumber())
                .remark(shipping.getRemark())
                .createdAt(shipping.getCreatedAt())
                .build();
    }

}
