package com.dsm.services;

import com.dsm.dto.request.RequestDTO.DashboardStats;
import com.dsm.entities.*;
import com.dsm.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CarrierRepository transporteurRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;
    private final ShippingRepository shippingRepository;
    private final InvoiceRepository invoiceRepository;

    //#region Main
    public DashboardStats getStats() {
        return DashboardStats.builder()
                .ordersCount(orderRepository.count())
                .ordersPendingApproval(orderRepository.getOrdersCountByStatus(Order.OrderStatus.pendingApproval))
                .ordersOngoing(orderRepository.getOrdersCountByStatus(Order.OrderStatus.ongoing))
                .ordersDelivered(orderRepository.getOrdersCountByStatus(Order.OrderStatus.delivered))
                .ordersCancelled(orderRepository.getOrdersCountByStatus(Order.OrderStatus.cancelled))
                .revenue(orderRepository.findAll().stream().map(Order::getTotalAmount).filter(amount -> amount != null).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue())
                .totalCustomers(customerRepository.count())
                .totalCarriers(transporteurRepository.count())
                .totalSuppliers(supplierRepository.count())
                .totalStocks(stockRepository.count())
                .shippingInPerparation(shippingRepository.getShippingsByStatus(Shipping.ShippingStatus.inTransit).size() + shippingRepository.getShippingsByStatus(Shipping.ShippingStatus.shipped).size())
                .invoicePending(invoiceRepository.getInvoicesByStatus(Invoice.InvoiceStatus.pending).size())
                .build();
    }
    //#endregion Main

}
