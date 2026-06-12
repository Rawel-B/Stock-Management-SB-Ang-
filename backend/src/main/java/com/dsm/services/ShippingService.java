package com.dsm.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dsm.dto.request.RequestDTO.ShippingRequest;
import com.dsm.dto.response.ResponseDTO.CarrierResponse;
import com.dsm.dto.response.ResponseDTO.ShippingResponse;
import com.dsm.entities.Carrier;
import com.dsm.entities.Order;
import com.dsm.entities.Shipping;
import com.dsm.exception.BusinessException;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.CarrierRepository;
import com.dsm.repositories.OrderRepository;
import com.dsm.repositories.ShippingRepository;

import lombok.RequiredArgsConstructor;

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

        if (order.getStatus() != Order.OrderStatus.validated && order.getStatus() != Order.OrderStatus.ongoing) {
            throw new BusinessException("Only Validated Or Ongoing Orders Can Have Deliveries.");
        }
        if (request.getCarrierId() != null) {
            carrier = carrierRepository.findById(request.getCarrierId()).orElseThrow(() -> new ResourceNotFoundException("Carrier Was Not Found."));
            if (!Boolean.TRUE.equals(carrier.getIsActive())) {
                throw new BusinessException("Inactive Carriers Cannot Be Assigned To Deliveries.");
            }
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

        order.setStatus(Order.OrderStatus.ongoing);
        orderRepository.save(order);

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
        validateStatusChange(shipping.getStatus(), status);
        boolean shouldDeductStock = shipping.getStatus() != Shipping.ShippingStatus.delivered && status == Shipping.ShippingStatus.delivered;
        shipping.setStatus(status);

        if (status == Shipping.ShippingStatus.delivered) {
            Order order = orderService.findById(shipping.getOrderId());
            if (shouldDeductStock) {
                stockService.deductOrder(order);
            }
            shipping.setReceiptDate(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.delivered);
            orderRepository.save(order);
        }

        return toResponse(shippingRepository.save(shipping));
    }
    public ShippingResponse updateShipping(String id, ShippingRequest request) {
        Shipping shipping = findShippingById(id);

        if (shipping.getStatus() != Shipping.ShippingStatus.inPerparation) {
            throw new BusinessException("Only Deliveries In Preparation Can Be Modified.");
        }
        if (request.getCarrierId() != null) {
            Carrier carrier = carrierRepository.findById(request.getCarrierId()).orElseThrow(() -> new ResourceNotFoundException("Carrier Was Not Found."));
            if (!Boolean.TRUE.equals(carrier.getIsActive())) {
                throw new BusinessException("Inactive Carriers Cannot Be Assigned To Deliveries.");
            }
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
        Shipping shipping = findShippingById(id);
        if (shipping.getStatus() != Shipping.ShippingStatus.inPerparation && shipping.getStatus() != Shipping.ShippingStatus.failed && shipping.getStatus() != Shipping.ShippingStatus.returned) {
            throw new BusinessException("Only Deliveries In Preparation, Failed, Or Returned Can Be Removed.");
        }
        shippingRepository.deleteById(id);
    }
    private void validateStatusChange(Shipping.ShippingStatus current, Shipping.ShippingStatus next) {
        if (current == next) {
            return;
        }
        boolean allowed = switch (current) {
            case inPerparation -> next == Shipping.ShippingStatus.shipped || next == Shipping.ShippingStatus.failed;
            case shipped -> next == Shipping.ShippingStatus.inTransit || next == Shipping.ShippingStatus.failed || next == Shipping.ShippingStatus.returned;
            case inTransit -> next == Shipping.ShippingStatus.delivered || next == Shipping.ShippingStatus.failed || next == Shipping.ShippingStatus.returned;
            case failed -> next == Shipping.ShippingStatus.inPerparation || next == Shipping.ShippingStatus.returned;
            case delivered, returned -> false;
        };
        if (!allowed) {
            throw new BusinessException("This Delivery Cannot Move To That Status.");
        }
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
