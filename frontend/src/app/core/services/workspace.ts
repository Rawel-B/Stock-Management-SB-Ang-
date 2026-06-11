import { Injectable } from '@angular/core';
import { Api } from './api';
import {
  CarrierRequest,
  CarrierResponse,
  CustomerRequest,
  CustomerResponse,
  DashboardStats,
  InvoiceRequest,
  InvoiceResponse,
  InvoiceStatus,
  LocationRequest,
  LocationResponse,
  OrderRequest,
  OrderResponse,
  OrderStatus,
  OrderSummaryResponse,
  PageResponse,
  ShippingRequest,
  ShippingResponse,
  ShippingStatus,
  StockRequest,
  StockResponse,
  SupportTicketRequest,
  SupportTicketResponse,
  SupportTicketStatusRequest,
  SupplierRequest,
  SupplierResponse,
  UserRequest,
  UserResponse
} from '../models/workspace';

@Injectable({
  providedIn: 'root'
})
export class Workspace {
  constructor(private readonly api: Api) {}

  getStats() {
    return this.api.get<DashboardStats>('/dashboard/stats');
  }

  getCustomers(criteria = '') {
    const query = criteria.trim() ? `?criteria=${encodeURIComponent(criteria.trim())}` : '';
    return this.api.get<CustomerResponse[]>(`/customers${query}`);
  }

  saveCustomer(request: CustomerRequest, id?: string) {
    return id ? this.api.put<CustomerResponse>(`/customers/${id}`, request) : this.api.post<CustomerResponse>('/customers', request);
  }
  deleteCustomer(id: string) {
    return this.api.delete<void>(`/customers/${id}`);
  }
  getCarriers() {
    return this.api.get<CarrierResponse[]>('/carriers');
  }
  saveCarrier(request: CarrierRequest, id?: string) {
    return id ? this.api.put<CarrierResponse>(`/carriers/${id}`, request) : this.api.post<CarrierResponse>('/carriers', request);
  }
  deleteCarrier(id: string) {
    return this.api.delete<void>(`/carriers/${id}`);
  }

  getSuppliers(criteria = '') {
    const query = criteria.trim() ? `?criteria=${encodeURIComponent(criteria.trim())}` : '';
    return this.api.get<SupplierResponse[]>(`/suppliers${query}`);
  }

  saveSupplier(request: SupplierRequest, id?: string) {
    return id ? this.api.put<SupplierResponse>(`/suppliers/${id}`, request) : this.api.post<SupplierResponse>('/suppliers', request);
  }

  deleteSupplier(id: string) {
    return this.api.delete<void>(`/suppliers/${id}`);
  }

  getStocks(criteria = '') {
    const query = criteria.trim() ? `?criteria=${encodeURIComponent(criteria.trim())}` : '';
    return this.api.get<StockResponse[]>(`/stocks${query}`);
  }

  getLocations(criteria = '') {
    const query = criteria.trim() ? `?criteria=${encodeURIComponent(criteria.trim())}` : '';
    return this.api.get<LocationResponse[]>(`/locations${query}`);
  }

  saveLocation(request: LocationRequest, id?: string) {
    return id ? this.api.put<LocationResponse>(`/locations/${id}`, request) : this.api.post<LocationResponse>('/locations', request);
  }

  deleteLocation(id: string) {
    return this.api.delete<void>(`/locations/${id}`);
  }

  saveStock(request: StockRequest, id?: string) {
    return id ? this.api.put<StockResponse>(`/stocks/${id}`, request) : this.api.post<StockResponse>('/stocks', request);
  }

  deleteStock(id: string) {
    return this.api.delete<void>(`/stocks/${id}`);
  }

  getOrders(status?: OrderStatus) {
    const query = status ? `?status=${status}` : '?page=0&size=100';
    return this.api.get<PageResponse<OrderSummaryResponse>>(`/orders${query}`);
  }

  getOrder(id: string) {
    return this.api.get<OrderResponse>(`/orders/${id}`);
  }

  saveOrder(request: OrderRequest, id?: string) {
    return id ? this.api.put<OrderResponse>(`/orders/${id}`, request) : this.api.post<OrderResponse>('/orders', request);
  }

  validateOrder(id: string) {
    return this.api.patch<OrderResponse>(`/orders/${id}/validateOrder`, {});
  }

  setOrderStatus(id: string, status: OrderStatus) {
    return this.api.patch<OrderResponse>(`/orders/${id}/setOrderStatus?statut=${status}`, {});
  }

  deleteOrder(id: string) {
    return this.api.delete<void>(`/orders/${id}`);
  }

  getShippings(status?: ShippingStatus) {
    const query = status ? `?status=${status}` : '';
    return this.api.get<ShippingResponse[]>(`/shippings${query}`);
  }

  saveShipping(request: ShippingRequest, id?: string) {
    return id ? this.api.put<ShippingResponse>(`/shippings/${id}`, request) : this.api.post<ShippingResponse>('/shippings', request);
  }

  setShippingStatus(id: string, status: ShippingStatus) {
    return this.api.patch<ShippingResponse>(`/shippings/${id}/status?statut=${status}`, {});
  }

  deleteShipping(id: string) {
    return this.api.delete<void>(`/shippings/${id}`);
  }

  getInvoices() {
    return this.api.get<InvoiceResponse[]>('/invoices');
  }

  saveInvoice(request: InvoiceRequest) {
    return this.api.post<InvoiceResponse>('/invoices', request);
  }

  setInvoiceStatus(id: string, status: InvoiceStatus) {
    return this.api.patch<InvoiceResponse>(`/invoices/${id}/status?status=${status}`, {});
  }

  deleteInvoice(id: string) {
    return this.api.delete<void>(`/invoices/${id}`);
  }

  getUsers() {
    return this.api.get<UserResponse[]>('/users');
  }

  saveUser(request: UserRequest, id?: string) {
    return id ? this.api.put<UserResponse>(`/users/${id}`, request) : this.api.post<UserResponse>('/users', request);
  }

  deleteUser(id: string) {
    return this.api.delete<void>(`/users/${id}`);
  }

  getSupportTickets(criteria = '') {
    const query = criteria.trim() ? `?criteria=${encodeURIComponent(criteria.trim())}` : '';
    return this.api.get<SupportTicketResponse[]>(`/support/tickets${query}`);
  }

  saveSupportTicket(request: SupportTicketRequest) {
    return this.api.post<SupportTicketResponse>('/support/tickets', request);
  }

  updateSupportTicket(id: string, request: SupportTicketStatusRequest) {
    return this.api.patch<SupportTicketResponse>(`/support/tickets/${id}`, request);
  }

  deleteSupportTicket(id: string) {
    return this.api.delete<void>(`/support/tickets/${id}`);
  }
}
