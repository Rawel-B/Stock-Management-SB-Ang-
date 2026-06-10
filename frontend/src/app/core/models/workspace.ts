export type OrderStatus = 'pendingApproval' | 'validated' | 'ongoing' | 'delivered' | 'cancelled';
export type ShippingStatus = 'inPerparation' | 'shipped' | 'inTransit' | 'delivered' | 'failed' | 'returned';
export type InvoiceStatus = 'pending' | 'processing' | 'completed' | 'failed' | 'refunded' | 'cancelled';
export type InvoicingMethod = 'creditCard' | 'debitCard' | 'bankTransfer' | 'Check' | 'Cash' | 'paypal' | 'stripe' | 'other';

export interface CustomerRequest {
  name: string;
  email: string;
  address?: string;
  phone?: string;
}

export interface CustomerResponse extends CustomerRequest {
  id: string;
  ordersCount: number;
  createdAt: string;
}

export interface CarrierRequest {
  name: string;
  phone?: string;
  rating?: number;
  isActive?: boolean;
}

export interface CarrierResponse extends CarrierRequest {
  id: string;
  shippingsCount: number;
  createdAt: string;
}

export interface SupplierRequest {
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  isActive?: boolean;
}

export interface SupplierResponse extends SupplierRequest {
  id: string;
  ordersCount: number;
  createdAt: string;
}

export interface StockRequest {
  product: string;
  productRef?: string;
  locationId?: string;
  quantity: number;
}

export interface StockResponse extends StockRequest {
  id: string;
  location?: string;
  lastReceiptDate?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface LocationRequest {
  name: string;
  code?: string;
  description?: string;
}

export interface LocationResponse extends LocationRequest {
  id: string;
  stockCount: number;
  createdAt: string;
  updatedAt?: string;
}

export interface ProductRequest {
  product: string;
  productRef?: string;
  quantity: number;
  pricePerUnit: number;
}

export interface ProductResponse extends ProductRequest {
  id: string;
  subTotal: number;
}

export interface OrderRequest {
  customerId: string;
  supplierId?: string;
  remark?: string;
  products: ProductRequest[];
}

export interface OrderSummaryResponse {
  id: string;
  orderNumber: string;
  customerName: string;
  supplierName?: string;
  orderDate: string;
  status: OrderStatus;
  totalAmount: number;
  ordersCount: number;
}

export interface OrderResponse extends OrderSummaryResponse {
  customer: CustomerResponse;
  supplier?: SupplierResponse;
  remark?: string;
  products: ProductResponse[];
  shippings: ShippingResponse[];
  invoices: InvoiceResponse[];
  createdAt: string;
  updatedAt?: string;
}

export interface ShippingRequest {
  orderId: string;
  carrierId?: string;
  deliveryDate?: string;
  cost?: number;
  shippingAddress?: string;
  trackingNumber?: string;
  remark?: string;
}

export interface ShippingResponse extends ShippingRequest {
  id: string;
  orderNumber: string;
  carrier?: CarrierResponse;
  receiptDate?: string;
  status: ShippingStatus;
  createdAt: string;
}

export interface InvoiceRequest {
  orderId: string;
  method: InvoicingMethod;
  amount: number;
  transactionRef?: string;
  remark?: string;
}

export interface InvoiceResponse {
  id: string;
  orderId: string;
  orderNumber: string;
  invoicingDate: string;
  invoiceStatus: InvoiceStatus;
  invoicingMethod: InvoicingMethod;
  amount: number;
  transactionRef?: string;
  remark?: string;
  createdAt: string;
}

export interface DashboardStats {
  ordersCount: number;
  ordersPendingApproval: number;
  ordersOngoing: number;
  ordersDelivered: number;
  ordersCancelled: number;
  revenue: number;
  totalCustomers: number;
  totalCarriers: number;
  totalSuppliers: number;
  totalStocks: number;
  shippingInPerparation: number;
  invoicePending: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
