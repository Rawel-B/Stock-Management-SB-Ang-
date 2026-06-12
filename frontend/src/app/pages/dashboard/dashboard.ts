import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, catchError, finalize, forkJoin, of, switchMap, timeout } from 'rxjs';
import { Auth } from '../../core/services/auth';
import { Workspace } from '../../core/services/workspace';
import { ProfileRequest } from '../../core/models/auth';
import { CarriersSection } from './components/carriers-section/carriers-section';
import { CustomersSection } from './components/customers-section/customers-section';
import { DeliveriesSection } from './components/deliveries-section/deliveries-section';
import { InvoicesSection } from './components/invoices-section/invoices-section';
import { LocationsSection } from './components/locations-section/locations-section';
import { OrdersSection } from './components/orders-section/orders-section';
import { OverviewSection } from './components/overview-section/overview-section';
import { ProfileSection } from './components/profile-section/profile-section';
import { StockSection } from './components/stock-section/stock-section';
import { SuppliersSection } from './components/suppliers-section/suppliers-section';
import { SupportSection } from './components/support-section/support-section';
import { UsersSection } from './components/users-section/users-section';
import {
  CarrierRequest,
  CarrierResponse,
  CustomerRequest,
  CustomerResponse,
  DashboardStats,
  InvoiceRequest,
  InvoiceResponse,
  InvoiceStatus,
  InvoicingMethod,
  LocationRequest,
  LocationResponse,
  OrderRequest,
  OrderResponse,
  OrderStatus,
  OrderSummaryResponse,
  PageResponse,
  ProductRequest,
  ShippingRequest,
  ShippingResponse,
  ShippingStatus,
  StockRequest,
  StockResponse,
  SupportCategory,
  SupportPriority,
  SupportStatus,
  SupportTicketRequest,
  SupportTicketResponse,
  SupplierRequest,
  SupplierResponse,
  UserRequest,
  UserResponse,
  UserRole
} from '../../core/models/workspace';

type Section = 'overview' | 'orders' | 'deliveries' | 'payments' | 'customers' | 'suppliers' | 'carriers' | 'stock' | 'locations' | 'profile' | 'support' | 'users';
type Confirmation = { title: string; message: string; confirmText: string; action: () => void };
type NavItem = { id: Section; label: string; icon: string };
type NavGroup = { label: string; items: NavItem[] };
type Toast = { type: 'success' | 'error'; message: string };
type FilterKey = 'customers' | 'suppliers' | 'carriers' | 'carrierStatus' | 'stock' | 'locations' | 'orders' | 'orderStatus' | 'shippingStatus' | 'invoiceStatus' | 'invoiceMethod' | 'users' | 'support' | 'supportStatus';
type DashboardFilterKey = 'year' | 'orderStatus' | 'shippingStatus' | 'invoiceStatus';
type WorkspaceData = {
  stats: DashboardStats;
  customers: CustomerResponse[];
  carriers: CarrierResponse[];
  suppliers: SupplierResponse[];
  stocks: StockResponse[];
  locations: LocationResponse[];
  orders: PageResponse<OrderSummaryResponse>;
  shippings: ShippingResponse[];
  invoices: InvoiceResponse[];
  users: UserResponse[];
  supportTickets: SupportTicketResponse[];
};

@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    FormsModule,
    CarriersSection,
    CustomersSection,
    DeliveriesSection,
    InvoicesSection,
    LocationsSection,
    OrdersSection,
    OverviewSection,
    ProfileSection,
    StockSection,
    SuppliersSection,
    SupportSection,
    UsersSection
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit, OnDestroy {
  readonly vm = this;
  readonly auth = inject(Auth);
  private readonly workspace = inject(Workspace);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
  private readonly activeSectionKey = 'dsm.activeSection';
  private readonly profileIconKey = 'dsm.profileIcon';
  private readonly requestTimeout = 15000;
  private readonly backendFilterKeys: FilterKey[] = ['customers', 'suppliers', 'stock', 'locations', 'orderStatus', 'shippingStatus', 'support'];
  readonly profileIcons = ['pi-user', 'pi-id-card', 'pi-briefcase', 'pi-shield', 'pi-box'];
  readonly sections: { id: Section; label: string }[] = [
    { id: 'overview', label: 'Dashboard' },
    { id: 'orders', label: 'Orders' },
    { id: 'deliveries', label: 'Deliveries' },
    { id: 'payments', label: 'Invoices' },
    { id: 'customers', label: 'Customers' },
    { id: 'suppliers', label: 'Suppliers' },
    { id: 'carriers', label: 'Carriers' },
    { id: 'stock', label: 'Stock' },
    { id: 'locations', label: 'Locations' },
    { id: 'users', label: 'Users' },
    { id: 'support', label: 'Support' }
  ];
  readonly navGroups: NavGroup[] = [
    {
      label: '',
      items: [
        { id: 'overview', label: 'Dashboard', icon: 'pi pi-chart-line' }
      ]
    },
    {
      label: 'Operations',
      items: [
        { id: 'orders', label: 'Orders', icon: 'pi pi-shopping-cart' },
        { id: 'deliveries', label: 'Deliveries', icon: 'pi pi-truck' },
        { id: 'payments', label: 'Invoices', icon: 'pi pi-file' }
      ]
    },
    {
      label: 'Inventory',
      items: [
        { id: 'stock', label: 'Stocks', icon: 'pi pi-box' },
        { id: 'locations', label: 'Locations', icon: 'pi pi-map-marker' }
      ]
    },
    {
      label: 'CRM',
      items: [
        { id: 'customers', label: 'Customers', icon: 'pi pi-users' },
        { id: 'suppliers', label: 'Suppliers', icon: 'pi pi-building' },
        { id: 'carriers', label: 'Carriers', icon: 'pi pi-send' },
      ]
    },
    {
      label: 'Support',
      items: [
        { id: 'users', label: 'Users', icon: 'pi pi-lock' },
        { id: 'support', label: 'Support', icon: 'pi pi-ticket' }
      ]
    }
  ];
  readonly orderStatuses: OrderStatus[] = ['pendingApproval', 'validated', 'ongoing', 'delivered', 'cancelled'];
  readonly orderWorkflowStatuses: OrderStatus[] = ['pendingApproval', 'validated', 'ongoing', 'delivered'];
  readonly shippingStatuses: ShippingStatus[] = ['inPerparation', 'shipped', 'inTransit', 'delivered', 'failed', 'returned'];
  readonly shippingWorkflowStatuses: ShippingStatus[] = ['inPerparation', 'shipped', 'inTransit', 'delivered'];
  readonly invoiceStatuses: InvoiceStatus[] = ['pending', 'processing', 'completed', 'failed', 'refunded', 'cancelled'];
  readonly invoiceWorkflowStatuses: InvoiceStatus[] = ['pending', 'processing', 'completed'];
  readonly invoicingMethods: InvoicingMethod[] = ['creditCard', 'debitCard', 'bankTransfer', 'Check', 'Cash', 'paypal', 'stripe', 'other'];
  readonly userRoles: UserRole[] = ['administrator', 'manager', 'user'];
  readonly supportCategories: SupportCategory[] = ['operations', 'account', 'data', 'technical', 'access', 'accountActivation'];
  readonly supportPriorities: SupportPriority[] = ['low', 'normal', 'high', 'urgent'];
  readonly supportStatuses: SupportStatus[] = ['open', 'inProgress', 'resolved', 'closed'];
  activeSection: Section = 'overview';
  loading = false;
  error = '';
  success = '';
  stats?: DashboardStats;
  customers: CustomerResponse[] = [];
  carriers: CarrierResponse[] = [];
  suppliers: SupplierResponse[] = [];
  stocks: StockResponse[] = [];
  locations: LocationResponse[] = [];
  orders: OrderSummaryResponse[] = [];
  shippings: ShippingResponse[] = [];
  invoices: InvoiceResponse[] = [];
  users: UserResponse[] = [];
  supportTickets: SupportTicketResponse[] = [];
  customerEditId = '';
  carrierEditId = '';
  supplierEditId = '';
  stockEditId = '';
  locationEditId = '';
  orderEditId = '';
  shippingEditId = '';
  userEditId = '';
  supportEditId = '';
  customerForm: CustomerRequest = this.emptyCustomer();
  carrierForm: CarrierRequest = this.emptyCarrier();
  supplierForm: SupplierRequest = this.emptySupplier();
  stockForm: StockRequest = this.emptyStock();
  locationForm: LocationRequest = this.emptyLocation();
  orderForm: OrderRequest = this.emptyOrder();
  productForm: ProductRequest = this.emptyProduct();
  shippingForm: ShippingRequest = this.emptyShipping();
  invoiceForm: InvoiceRequest = this.emptyInvoice();
  userForm: UserRequest = this.emptyUser();
  supportStatusForm: Record<string, { status: SupportStatus; assignedUserId: string }> = {};
  profileForm: ProfileRequest = this.emptyProfile();
  supportForm = this.emptySupport();
  filters: Record<FilterKey, string> = {
    customers: '',
    suppliers: '',
    carriers: '',
    carrierStatus: '',
    stock: '',
    locations: '',
    orders: '',
    orderStatus: '',
    shippingStatus: '',
    invoiceStatus: '',
    invoiceMethod: '',
    users: '',
    support: '',
    supportStatus: ''
  };
  dashboardFilters = {
    year: '',
    orderStatus: '',
    shippingStatus: '',
    invoiceStatus: ''
  };
  confirmation: Confirmation | null = null;
  toast: Toast | null = null;
  accountMenuOpen = false;
  profileIcon = this.profileIcons[0];
  formSections: Partial<Record<Section, boolean>> = {};
  expandedRows: Partial<Record<Section, string>> = {};
  orderDetails: Record<string, OrderResponse> = {};
  orderDetailsLoading: Record<string, boolean> = {};
  private toastTimer?: ReturnType<typeof setTimeout>;

  ngOnInit() {
    this.restoreActiveSection();
    this.restoreProfileIcon();
    this.setProfileForm();
    this.auth.profile().subscribe({
      next: () => {
        this.setProfileForm();
        this.ensureAllowedSection();
        this.updateView();
      },
      error: () => {
        this.setProfileForm();
        this.ensureAllowedSection();
        this.updateView();
      }
    });
    this.loadAll();
  }

  ngOnDestroy() {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
  }

  signOut() {
    localStorage.removeItem(this.activeSectionKey);
    this.auth.signOut();
  }

  openProfile() {
    this.setProfileForm();
    this.accountMenuOpen = false;
    this.setSection('profile');
  }

  toggleAccountMenu() {
    this.accountMenuOpen = !this.accountMenuOpen;
  }

  saveProfile() {
    const request: ProfileRequest = {
      username: this.profileForm.username.trim(),
      name: this.profileForm.name.trim(),
      email: this.profileForm.email.trim().toLowerCase(),
      password: this.profileForm.password?.trim() || undefined
    };
    if (!request.name || !request.username || !request.email) {
      this.showError('Fill all required profile fields.');
      return;
    }
    if (!this.emailPattern.test(request.email)) {
      this.showError('Enter a valid email address.');
      return;
    }
    this.loading = true;
    this.clearMessages();
    this.auth.updateProfile(request)
      .pipe(timeout(this.requestTimeout), finalize(() => {
        this.loading = false;
        this.updateView();
      }))
      .subscribe({
        next: () => {
          this.profileForm.password = '';
          this.showSuccess('Profile updated.');
        },
        error: error => this.showError(this.readError(error))
      });
  }

  setSection(section: Section) {
    if (!this.canAccessSection(section)) {
      this.activeSection = this.defaultSection();
      this.saveActiveSection(this.activeSection);
      return;
    }
    this.activeSection = section;
    this.saveActiveSection(section);
    this.accountMenuOpen = false;
    this.clearMessages();
  }

  visibleNavGroups() {
    return this.navGroups
      .map(group => ({ ...group, items: group.items.filter(item => this.canAccessSection(item.id)) }))
      .filter(group => group.items.length);
  }

  canAccessSection(section: Section) {
    const role = this.currentRole();
    if (section === 'profile') {
      return true;
    }
    if (role === 'administrator') {
      return true;
    }
    if (role === 'manager') {
      return ['overview', 'orders', 'deliveries', 'payments', 'customers', 'suppliers', 'carriers'].includes(section);
    }
    return ['deliveries', 'stock', 'locations'].includes(section);
  }

  currentRole(): UserRole {
    const role = this.auth.user()?.role as UserRole | undefined;
    return role && this.userRoles.includes(role) ? role : 'user';
  }

  isAdmin() {
    return this.currentRole() === 'administrator';
  }

  defaultSection(): Section {
    if (this.isAdmin() || this.currentRole() === 'manager') {
      return 'overview';
    }
    return 'deliveries';
  }

  formVisible(section: Section) {
    return !!this.formSections[section];
  }

  openCreate(section: Section) {
    if (section === 'customers') {
      this.customerForm = this.emptyCustomer();
      this.customerEditId = '';
    }
    if (section === 'suppliers') {
      this.supplierForm = this.emptySupplier();
      this.supplierEditId = '';
    }
    if (section === 'carriers') {
      this.carrierForm = this.emptyCarrier();
      this.carrierEditId = '';
    }
    if (section === 'stock') {
      this.stockForm = this.emptyStock();
      this.stockEditId = '';
    }
    if (section === 'locations') {
      this.locationForm = this.emptyLocation();
      this.locationEditId = '';
    }
    if (section === 'orders') {
      this.orderForm = this.emptyOrder();
      this.orderEditId = '';
    }
    if (section === 'deliveries') {
      this.shippingForm = this.emptyShipping();
      this.shippingEditId = '';
    }
    if (section === 'payments') {
      this.invoiceForm = this.emptyInvoice();
    }
    if (section === 'users') {
      this.userForm = this.emptyUser();
      this.userEditId = '';
    }
    if (section === 'support') {
      this.supportForm = this.emptySupport();
      this.supportEditId = '';
    }
    this.formSections[section] = true;
  }

  closeForm(section: Section) {
    this.formSections[section] = false;
  }

  currentSectionLabel() {
    if (this.activeSection === 'profile') {
      return 'Profile';
    }
    if (this.activeSection === 'support') {
      return 'Support';
    }
    if (this.activeSection === 'users') {
      return 'Users';
    }
    return this.sections.find(section => section.id === this.activeSection)?.label ?? 'Dashboard';
  }

  userInitials() {
    const name = this.auth.user()?.name?.trim() || this.auth.user()?.username?.trim() || 'DSM';
    return name.split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase();
  }

  profileIconClass() {
    return `pi ${this.profileIcon}`;
  }

  selectProfileIcon(icon: string) {
    this.profileIcon = icon;
    localStorage.setItem(this.profileIconKey, icon);
  }

  applyFilters() {
    this.loadAll();
  }

  clearFilter(key: FilterKey) {
    this.filters[key] = '';
    if (this.backendFilterKeys.includes(key)) {
      this.loadAll();
      return;
    }
    this.updateView();
  }

  filteredCarriers() {
    const criteria = this.filters.carriers.trim().toLowerCase();
    const status = this.filters.carrierStatus;

    return this.carriers.filter(carrier => {
      const matchesCriteria = !criteria
        || carrier.name.toLowerCase().includes(criteria)
        || (carrier.phone ?? '').toLowerCase().includes(criteria);
      const matchesStatus = !status
        || status === 'active' && carrier.isActive
        || status === 'inactive' && !carrier.isActive;

      return matchesCriteria && matchesStatus;
    });
  }

  filteredInvoices() {
    return this.invoices.filter(invoice => {
      const matchesStatus = !this.filters.invoiceStatus || invoice.invoiceStatus === this.filters.invoiceStatus;
      const matchesMethod = !this.filters.invoiceMethod || invoice.invoicingMethod === this.filters.invoiceMethod;

      return matchesStatus && matchesMethod;
    });
  }

  filteredOrders() {
    const criteria = this.filters.orders.trim().toLowerCase();

    return this.orders.filter(order => !criteria
      || order.orderNumber.toLowerCase().includes(criteria)
      || order.customerName.toLowerCase().includes(criteria)
      || (order.supplierName ?? '').toLowerCase().includes(criteria)
      || this.orderStatusLabel(order.status).toLowerCase().includes(criteria)
      || order.totalAmount.toString().includes(criteria));
  }

  dashboardOrders() {
    return this.dashboardOrderScope();
  }

  dashboardShippings() {
    return this.dashboardShippingScope();
  }

  dashboardInvoices() {
    return this.dashboardInvoiceScope();
  }

  dashboardOrderStatusOptions() {
    return this.orderStatuses;
  }

  dashboardShippingStatusOptions() {
    return this.shippingStatuses;
  }

  dashboardInvoiceStatusOptions() {
    return this.invoiceStatuses;
  }

  dashboardCards() {
    const shippings = this.dashboardShippings();
    const orders = this.dashboardOrders();
    const invoices = this.dashboardInvoices();
    const collectedRevenue = invoices.filter(invoice => invoice.invoiceStatus === 'completed').reduce((total, invoice) => total + invoice.amount, 0);
    const outstandingValue = invoices.filter(invoice => invoice.invoiceStatus === 'pending' || invoice.invoiceStatus === 'processing').reduce((total, invoice) => total + invoice.amount, 0);
    const openOrders = orders.filter(order => order.status !== 'delivered' && order.status !== 'cancelled').length;
    const approvalQueue = orders.filter(order => order.status === 'pendingApproval').length;
    const deliveredShippings = shippings.filter(shipping => shipping.status === 'delivered').length;
    const deliveryRate = shippings.length ? this.percent(deliveredShippings, shippings.length) : 0;
    const stockUnits = this.stocks.reduce((total, stock) => total + stock.quantity, 0);
    const assignedStockUnits = this.stocks.filter(stock => stock.location).reduce((total, stock) => total + stock.quantity, 0);

    return [
      { label: 'Collected Revenue', value: this.currencyLabel(collectedRevenue), detail: `${invoices.filter(invoice => invoice.invoiceStatus === 'completed').length} completed invoices`, icon: 'pi pi-dollar' },
      { label: 'Open Orders', value: openOrders.toString(), detail: `${orders.length} total orders`, icon: 'pi pi-shopping-cart' },
      { label: 'Approval Queue', value: approvalQueue.toString(), detail: 'Orders waiting validation', icon: 'pi pi-check-circle' },
      { label: 'Delivery Completion', value: `${deliveryRate}%`, detail: `${deliveredShippings}/${shippings.length} delivered`, icon: 'pi pi-truck' },
      { label: 'Stock Units', value: stockUnits.toString(), detail: `${assignedStockUnits} assigned to locations`, icon: 'pi pi-box' },
      { label: 'Outstanding Value', value: this.currencyLabel(outstandingValue), detail: 'Pending or processing invoices', icon: 'pi pi-wallet' }
    ];
  }

  dashboardOrderBreakdown() {
    const orders = this.dashboardOrders();
    return this.orderStatuses.map(status => ({
      label: this.orderStatusLabel(status),
      value: orders.filter(order => order.status === status).length
    }));
  }

  dashboardCarrierBreakdown() {
    const shippings = this.dashboardShippings();
    const names = [...new Set(shippings.map(shipping => shipping.carrier?.name || 'No Carrier'))];
    return names.map(name => ({
      label: name,
      value: shippings.filter(shipping => (shipping.carrier?.name || 'No Carrier') === name).length
    }));
  }

  dashboardLocationBreakdown() {
    const names = [...new Set(this.stocks.map(stock => stock.location || 'No Location'))];
    return names.map(name => ({
      label: name,
      value: this.stocks.filter(stock => (stock.location || 'No Location') === name).reduce((total, stock) => total + stock.quantity, 0)
    }));
  }

  dashboardInvoiceBreakdown() {
    const invoices = this.dashboardInvoices();
    return this.invoiceStatuses.map(status => ({
      label: this.invoiceStatusLabel(status),
      value: invoices.filter(invoice => invoice.invoiceStatus === status).length
    }));
  }

  dashboardCustomerOrderValueBreakdown() {
    const names = [...new Set(this.dashboardOrders().map(order => order.customerName || 'No Customer'))];
    return names.map(name => ({
      label: name,
      value: this.dashboardOrders().filter(order => (order.customerName || 'No Customer') === name).length,
      amount: this.dashboardOrders().filter(order => (order.customerName || 'No Customer') === name).reduce((total, order) => total + order.totalAmount, 0)
    })).sort((first, second) => second.amount - first.amount).slice(0, 8);
  }

  dashboardTopCustomersRevenue() {
    return this.dashboardCustomerOrderValueBreakdown().map(item => ({
      label: item.label,
      orders: item.value,
      revenue: item.amount,
      average: item.value ? item.amount / item.value : 0
    }));
  }

  dashboardFulfillmentQueue() {
    const orders = this.dashboardOrders();
    const shippings = this.dashboardShippings();
    const invoices = this.dashboardInvoices();

    return [
      { label: 'Awaiting Approval', value: orders.filter(order => order.status === 'pendingApproval').length, detail: 'Orders' },
      { label: 'Ready For Delivery', value: orders.filter(order => order.status === 'validated').length, detail: 'Orders' },
      { label: 'Preparing', value: shippings.filter(shipping => shipping.status === 'inPerparation').length, detail: 'Deliveries' },
      { label: 'In Transit', value: shippings.filter(shipping => shipping.status === 'shipped' || shipping.status === 'inTransit').length, detail: 'Deliveries' },
      { label: 'Delivered', value: orders.filter(order => order.status === 'delivered').length, detail: 'Orders' },
      { label: 'Payment Pending', value: invoices.filter(invoice => invoice.invoiceStatus === 'pending' || invoice.invoiceStatus === 'processing').length, detail: 'Invoices' }
    ];
  }

  dashboardFulfillmentTotal() {
    return this.dashboardFulfillmentQueue().reduce((total, item) => total + item.value, 0);
  }

  dashboardFulfillmentBottleneck() {
    return this.dashboardFulfillmentQueue().reduce((highest, item) => item.value > highest.value ? item : highest, { label: 'No Workload', value: 0, detail: '' });
  }

  dashboardRevenueMetrics() {
    const invoices = this.dashboardInvoices();
    const completed = invoices.filter(invoice => invoice.invoiceStatus === 'completed').reduce((total, invoice) => total + invoice.amount, 0);
    const pending = invoices.filter(invoice => invoice.invoiceStatus === 'pending').reduce((total, invoice) => total + invoice.amount, 0);
    const processing = invoices.filter(invoice => invoice.invoiceStatus === 'processing').reduce((total, invoice) => total + invoice.amount, 0);
    const failed = invoices.filter(invoice => invoice.invoiceStatus === 'failed' || invoice.invoiceStatus === 'cancelled' || invoice.invoiceStatus === 'refunded').reduce((total, invoice) => total + invoice.amount, 0);

    return [
      { label: 'Completed', value: this.currencyLabel(completed) },
      { label: 'Pending', value: this.currencyLabel(pending) },
      { label: 'Processing', value: this.currencyLabel(processing) },
      { label: 'Uncollected', value: this.currencyLabel(failed) }
    ];
  }

  dashboardInvoiceValueBreakdown() {
    const invoices = this.dashboardInvoices();
    return this.invoiceStatuses.map(status => ({
      label: this.invoiceStatusLabel(status),
      value: invoices.filter(invoice => invoice.invoiceStatus === status).reduce((total, invoice) => total + invoice.amount, 0)
    }));
  }

  dashboardYears() {
    const currentYear = new Date().getFullYear();
    const firstYear = 2020;

    return Array.from({ length: currentYear - firstYear + 1 }, (_, index) => currentYear - index);
  }

  dashboardSelectedYear() {
    const selected = Number(this.dashboardFilters.year);
    return selected || null;
  }

  dashboardMonthlyOrders() {
    const year = this.dashboardSelectedYear();
    return this.monthlySeries(index => this.dashboardOrders().filter(order => {
      const date = new Date(order.orderDate);
      return (!year || date.getFullYear() === year) && date.getMonth() === index;
    }).length);
  }

  dashboardMonthlyDeliveries() {
    const year = this.dashboardSelectedYear();
    return this.monthlySeries(index => this.dashboardShippings().filter(shipping => {
      const date = new Date(shipping.deliveryDate || shipping.createdAt);
      return (!year || date.getFullYear() === year) && date.getMonth() === index;
    }).length);
  }

  dashboardMonthlyCollectedRevenue() {
    const year = this.dashboardSelectedYear();
    return this.monthlySeries(index => this.dashboardInvoices().filter(invoice => {
      const date = new Date(invoice.invoicingDate);
      return invoice.invoiceStatus === 'completed' && (!year || date.getFullYear() === year) && date.getMonth() === index;
    }).reduce((total, invoice) => total + invoice.amount, 0));
  }

  dashboardCarrierRatings() {
    return [...this.carriers]
      .sort((first, second) => (second.rating ?? 0) - (first.rating ?? 0))
      .slice(0, 5)
      .map(carrier => ({ label: carrier.name, value: carrier.rating ?? 0 }));
  }

  linePoints(items: { value: number }[]) {
    const max = this.lineScaleMax(items);

    return items.map((item, index) => {
      const x = this.chartX(index, items.length);
      const y = this.chartY(item.value, max);
      return `${x},${y}`;
    }).join(' ');
  }

  lineAreaPoints(items: { value: number }[]) {
    const points = this.linePoints(items);
    return points ? `44,136 ${points} 300,136` : '';
  }

  lineMaxLabel(items: { value: number }[]) {
    return Math.max(...items.map(item => item.value), 0);
  }

  lineMarkers(items: { label: string; value: number }[]) {
    const max = this.lineScaleMax(items);
    return items.map((item, index) => ({
      label: item.label,
      value: item.value,
      x: this.chartX(index, items.length),
      y: this.chartY(item.value, max)
    }));
  }

  lineTicks(items: { value: number }[]) {
    const max = this.lineScaleMax(items);
    const values = max <= 4
      ? Array.from({ length: max + 1 }, (_, index) => max - index)
      : [max, max * 0.75, max * 0.5, max * 0.25, 0];

    return values.map(value => ({ value, y: this.chartY(value, max) }));
  }

  lineMonthLabels(items: { label: string; value: number }[]) {
    return items
      .map((item, index) => ({ label: item.label, x: this.chartX(index, items.length) }))
      .filter((_, index) => index === 0 || index === 2 || index === 5 || index === 8 || index === 11);
  }

  lineTickLabel(value: number, currency = false) {
    if (!currency) {
      return Math.round(value).toString();
    }
    if (value >= 1000000) {
      return `$${(value / 1000000).toFixed(1)}M`;
    }
    if (value >= 1000) {
      return `$${(value / 1000).toFixed(1)}K`;
    }
    return this.currencyLabel(value);
  }

  private monthlySeries(value: (monthIndex: number) => number) {
    return ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'].map((label, index) => ({ label, value: value(index) }));
  }

  private chartX(index: number, total: number) {
    return 44 + (total === 1 ? 0 : index * (256 / (total - 1)));
  }

  private chartY(value: number, max: number) {
    return 136 - (value / Math.max(max, 1)) * 112;
  }

  private lineScaleMax(items: { value: number }[]) {
    const highest = Math.max(...items.map(item => item.value), 0);
    if (highest <= 4) {
      return Math.max(Math.ceil(highest), 1);
    }
    const magnitude = Math.pow(10, Math.floor(Math.log10(highest)));
    const normalized = highest / magnitude;
    const rounded = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10;

    return rounded * magnitude;
  }

  private dashboardOrderScope(ignore?: DashboardFilterKey) {
    const shippingOrderIds = this.dashboardShippingOrderIds(ignore);
    const invoiceOrderIds = this.dashboardInvoiceOrderIds(ignore);

    return this.orders.filter(order => {
      const date = new Date(order.orderDate);
      const matchesYear = ignore === 'year' || !this.dashboardSelectedYear() || date.getFullYear() === this.dashboardSelectedYear();
      const matchesStatus = ignore === 'orderStatus' || !this.dashboardFilters.orderStatus || order.status === this.dashboardFilters.orderStatus;
      const matchesShipping = ignore === 'shippingStatus' || !this.dashboardFilters.shippingStatus || shippingOrderIds.has(order.id);
      const matchesInvoice = ignore === 'invoiceStatus' || !this.dashboardFilters.invoiceStatus || invoiceOrderIds.has(order.id);

      return matchesYear && matchesStatus && matchesShipping && matchesInvoice;
    });
  }

  private dashboardShippingScope(ignore?: DashboardFilterKey) {
    const orderIds = this.dashboardOrderIds(ignore);
    const invoiceOrderIds = this.dashboardInvoiceOrderIds(ignore);

    return this.shippings.filter(shipping => {
      const date = new Date(shipping.deliveryDate || shipping.createdAt);
      const matchesYear = ignore === 'year' || !this.dashboardSelectedYear() || date.getFullYear() === this.dashboardSelectedYear();
      const matchesStatus = ignore === 'shippingStatus' || !this.dashboardFilters.shippingStatus || shipping.status === this.dashboardFilters.shippingStatus;
      const matchesOrder = ignore === 'orderStatus' || !this.dashboardFilters.orderStatus || orderIds.has(shipping.orderId);
      const matchesInvoice = ignore === 'invoiceStatus' || !this.dashboardFilters.invoiceStatus || invoiceOrderIds.has(shipping.orderId);

      return matchesYear && matchesStatus && matchesOrder && matchesInvoice;
    });
  }

  private dashboardInvoiceScope(ignore?: DashboardFilterKey) {
    const orderIds = this.dashboardOrderIds(ignore);
    const shippingOrderIds = this.dashboardShippingOrderIds(ignore);

    return this.invoices.filter(invoice => {
      const date = new Date(invoice.invoicingDate);
      const matchesYear = ignore === 'year' || !this.dashboardSelectedYear() || date.getFullYear() === this.dashboardSelectedYear();
      const matchesStatus = ignore === 'invoiceStatus' || !this.dashboardFilters.invoiceStatus || invoice.invoiceStatus === this.dashboardFilters.invoiceStatus;
      const matchesOrder = ignore === 'orderStatus' || !this.dashboardFilters.orderStatus || orderIds.has(invoice.orderId);
      const matchesShipping = ignore === 'shippingStatus' || !this.dashboardFilters.shippingStatus || shippingOrderIds.has(invoice.orderId);

      return matchesYear && matchesStatus && matchesOrder && matchesShipping;
    });
  }

  private dashboardOrderIds(ignore?: DashboardFilterKey) {
    return new Set(this.orders.filter(order => {
      const date = new Date(order.orderDate);
      const matchesYear = ignore === 'year' || !this.dashboardSelectedYear() || date.getFullYear() === this.dashboardSelectedYear();
      const matchesStatus = ignore === 'orderStatus' || !this.dashboardFilters.orderStatus || order.status === this.dashboardFilters.orderStatus;

      return matchesYear && matchesStatus;
    }).map(order => order.id));
  }

  private dashboardShippingOrderIds(ignore?: DashboardFilterKey) {
    return new Set(this.shippings.filter(shipping => {
      const date = new Date(shipping.deliveryDate || shipping.createdAt);
      const matchesYear = ignore === 'year' || !this.dashboardSelectedYear() || date.getFullYear() === this.dashboardSelectedYear();
      const matchesStatus = ignore === 'shippingStatus' || !this.dashboardFilters.shippingStatus || shipping.status === this.dashboardFilters.shippingStatus;

      return matchesYear && matchesStatus;
    }).map(shipping => shipping.orderId));
  }

  private dashboardInvoiceOrderIds(ignore?: DashboardFilterKey) {
    return new Set(this.invoices.filter(invoice => {
      const date = new Date(invoice.invoicingDate);
      const matchesYear = ignore === 'year' || !this.dashboardSelectedYear() || date.getFullYear() === this.dashboardSelectedYear();
      const matchesStatus = ignore === 'invoiceStatus' || !this.dashboardFilters.invoiceStatus || invoice.invoiceStatus === this.dashboardFilters.invoiceStatus;

      return matchesYear && matchesStatus;
    }).map(invoice => invoice.orderId));
  }

  private matchingStatuses<T extends string>(statuses: T[], values: T[], selected: string) {
    const available = new Set(values);

    return statuses.filter(status => available.has(status) || status === selected);
  }

  orderBreakdown() {
    return [
      { label: 'Pending Approval', value: this.stats?.ordersPendingApproval ?? 0 },
      { label: 'Ongoing', value: this.stats?.ordersOngoing ?? 0 },
      { label: 'Delivered', value: this.stats?.ordersDelivered ?? 0 },
      { label: 'Cancelled', value: this.stats?.ordersCancelled ?? 0 }
    ];
  }

  resourceBreakdown() {
    return [
      { label: 'Customers', value: this.stats?.totalCustomers ?? 0 },
      { label: 'Suppliers', value: this.stats?.totalSuppliers ?? 0 },
      { label: 'Carriers', value: this.stats?.totalCarriers ?? 0 },
      { label: 'Stock items', value: this.stats?.totalStocks ?? 0 }
    ];
  }

  queueBreakdown() {
    return [
      { label: 'Delivery Preparation', value: this.stats?.shippingInPerparation ?? 0 },
      { label: 'Pending Invoices', value: this.stats?.invoicePending ?? 0 },
      { label: 'Open Orders', value: this.openOrdersCount() }
    ];
  }

  recentOrders() {
    return this.orders.slice(0, 5);
  }

  orderStatusLabel(status: OrderStatus) {
    return this.enumLabel(status);
  }

  shippingStatusLabel(status: ShippingStatus) {
    return this.enumLabel(status === 'inPerparation' ? 'inPreparation' : status);
  }

  invoiceStatusLabel(status: InvoiceStatus) {
    return this.enumLabel(status);
  }

  methodLabel(method: InvoicingMethod) {
    return this.enumLabel(method);
  }

  displayLabel(value?: string) {
    return value ? this.enumLabel(value) : '';
  }

  currencyLabel(value: number) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
  }

  isRowExpanded(section: Section, id: string) {
    return this.expandedRows[section] === id;
  }

  toggleRow(section: Section, id: string) {
    this.expandedRows[section] = this.isRowExpanded(section, id) ? '' : id;

    if (section === 'orders' && this.expandedRows[section] === id && !this.orderDetails[id] && !this.orderDetailsLoading[id]) {
      this.loadOrderDetails(id);
    }

    this.updateView();
  }

  private loadOrderDetails(id: string) {
    this.orderDetailsLoading[id] = true;
    this.workspace.getOrder(id)
      .pipe(timeout(this.requestTimeout), finalize(() => {
        this.orderDetailsLoading[id] = false;
        this.updateView();
      }))
      .subscribe({
        next: response => {
          this.orderDetails[id] = response;
          this.updateView();
        },
        error: error => this.showError(this.readError(error))
      });
  }

  orderStepDone(current: OrderStatus, status: OrderStatus) {
    return current !== 'cancelled' && this.orderWorkflowStatuses.indexOf(status) < this.orderWorkflowStatuses.indexOf(current);
  }

  orderStepAvailable(current: OrderStatus, status: OrderStatus) {
    return current === 'pendingApproval' && status === 'validated' || current === 'validated' && status === 'ongoing';
  }

  orderCanCancel(status: OrderStatus) {
    return status === 'pendingApproval' || status === 'validated';
  }

  orderCanDelete(status: OrderStatus) {
    return status === 'pendingApproval' || status === 'cancelled';
  }

  orderStepTooltip(current: OrderStatus, status: OrderStatus) {
    if (current === status) {
      return this.orderStatusLabel(status);
    }
    if (current === 'pendingApproval' && status === 'validated') {
      return 'Validate Order';
    }
    if (current === 'validated' && status === 'ongoing') {
      return 'Create Delivery';
    }
    if (status === 'delivered') {
      return 'Complete From Delivery';
    }
    return this.orderStatusLabel(status);
  }

  orderHasNextAction(status: OrderStatus) {
    return status === 'pendingApproval' || status === 'validated';
  }

  orderNextActionLabel(status: OrderStatus) {
    if (status === 'pendingApproval') {
      return 'Validate Order';
    }
    if (status === 'validated') {
      return 'Create Delivery';
    }
    return '';
  }

  orderNextActionIcon(status: OrderStatus) {
    if (status === 'pendingApproval') {
      return 'pi pi-check';
    }
    if (status === 'validated') {
      return 'pi pi-truck';
    }
    return 'pi pi-arrow-right';
  }

  runOrderNext(order: OrderSummaryResponse) {
    if (order.status === 'pendingApproval') {
      this.validateOrder(order.id);
      return;
    }
    if (order.status === 'validated') {
      this.openDeliveryForOrder(order);
    }
  }

  shippingStepDone(current: ShippingStatus, status: ShippingStatus) {
    return current !== 'failed' && current !== 'returned' && this.shippingWorkflowStatuses.indexOf(status) < this.shippingWorkflowStatuses.indexOf(current);
  }

  shippingStepAvailable(current: ShippingStatus, status: ShippingStatus) {
    return current === 'inPerparation' && status === 'shipped'
      || current === 'shipped' && status === 'inTransit'
      || current === 'inTransit' && status === 'delivered'
      || current === 'failed' && status === 'inPerparation';
  }

  shippingCanFail(status: ShippingStatus) {
    return status === 'inPerparation' || status === 'shipped' || status === 'inTransit';
  }

  shippingCanReturn(status: ShippingStatus) {
    return status === 'shipped' || status === 'inTransit' || status === 'failed';
  }

  shippingStepTooltip(current: ShippingStatus, status: ShippingStatus) {
    if (current === status) {
      return this.shippingStatusLabel(status);
    }
    if (current === 'failed' && status === 'inPerparation') {
      return 'Prepare Again';
    }
    return `Move To ${this.shippingStatusLabel(status)}`;
  }

  shippingNextStatus(status: ShippingStatus): ShippingStatus | null {
    if (status === 'inPerparation') {
      return 'shipped';
    }
    if (status === 'shipped') {
      return 'inTransit';
    }
    if (status === 'inTransit') {
      return 'delivered';
    }
    if (status === 'failed') {
      return 'inPerparation';
    }
    return null;
  }

  shippingNextActionLabel(status: ShippingStatus) {
    if (status === 'inPerparation') {
      return 'Ship Delivery';
    }
    if (status === 'shipped') {
      return 'Start Transit';
    }
    if (status === 'inTransit') {
      return 'Confirm Delivered';
    }
    if (status === 'failed') {
      return 'Prepare Again';
    }
    return '';
  }

  shippingNextActionIcon(status: ShippingStatus) {
    if (status === 'inPerparation') {
      return 'pi pi-send';
    }
    if (status === 'shipped') {
      return 'pi pi-truck';
    }
    if (status === 'inTransit') {
      return 'pi pi-check';
    }
    if (status === 'failed') {
      return 'pi pi-replay';
    }
    return 'pi pi-arrow-right';
  }

  runShippingNext(shipping: ShippingResponse) {
    const status = this.shippingNextStatus(shipping.status);

    if (status) {
      this.processShipping(shipping, status);
    }
  }

  processShipping(shipping: ShippingResponse, status: ShippingStatus) {
    if (shipping.status === status) {
      return;
    }
    if (!this.shippingStepAvailable(shipping.status, status)) {
      this.showError('This delivery cannot move to that step.');
      return;
    }
    this.setShippingStatus(shipping.id, status);
  }

  failShipping(shipping: ShippingResponse) {
    if (!this.shippingCanFail(shipping.status)) {
      this.showError('This delivery cannot be marked as failed.');
      return;
    }
    this.run(this.workspace.setShippingStatus(shipping.id, 'failed'), 'Delivery marked as failed.');
  }

  returnShipping(shipping: ShippingResponse) {
    if (!this.shippingCanReturn(shipping.status)) {
      this.showError('This delivery cannot be returned.');
      return;
    }
    this.run(this.workspace.setShippingStatus(shipping.id, 'returned'), 'Delivery returned.');
  }

  invoiceStepDone(current: InvoiceStatus, status: InvoiceStatus) {
    return current !== 'failed' && current !== 'refunded' && current !== 'cancelled' && this.invoiceWorkflowStatuses.indexOf(status) < this.invoiceWorkflowStatuses.indexOf(current);
  }

  invoiceStepAvailable(current: InvoiceStatus, status: InvoiceStatus) {
    return current === 'pending' && status === 'processing'
      || current === 'processing' && status === 'completed'
      || current === 'failed' && status === 'processing';
  }

  invoiceCanFail(status: InvoiceStatus) {
    return status === 'processing';
  }

  invoiceCanCancel(status: InvoiceStatus) {
    return status === 'pending' || status === 'processing' || status === 'failed';
  }

  invoiceCanRefund(status: InvoiceStatus) {
    return status === 'completed';
  }

  invoiceStepTooltip(current: InvoiceStatus, status: InvoiceStatus) {
    if (current === status) {
      return this.invoiceStatusLabel(status);
    }
    if (current === 'failed' && status === 'processing') {
      return 'Retry Processing';
    }
    return `Move To ${this.invoiceStatusLabel(status)}`;
  }

  invoiceNextStatus(status: InvoiceStatus): InvoiceStatus | null {
    if (status === 'pending') {
      return 'processing';
    }
    if (status === 'processing') {
      return 'completed';
    }
    if (status === 'failed') {
      return 'processing';
    }
    return null;
  }

  invoiceNextActionLabel(status: InvoiceStatus) {
    if (status === 'pending') {
      return 'Start Processing';
    }
    if (status === 'processing') {
      return 'Mark Paid';
    }
    if (status === 'failed') {
      return 'Retry Processing';
    }
    return '';
  }

  invoiceNextActionIcon(status: InvoiceStatus) {
    if (status === 'pending') {
      return 'pi pi-play';
    }
    if (status === 'processing') {
      return 'pi pi-check';
    }
    if (status === 'failed') {
      return 'pi pi-replay';
    }
    return 'pi pi-arrow-right';
  }

  runInvoiceNext(invoice: InvoiceResponse) {
    const status = this.invoiceNextStatus(invoice.invoiceStatus);

    if (status) {
      this.processInvoice(invoice, status);
    }
  }

  processInvoice(invoice: InvoiceResponse, status: InvoiceStatus) {
    if (invoice.invoiceStatus === status) {
      return;
    }
    if (!this.invoiceStepAvailable(invoice.invoiceStatus, status)) {
      this.showError('This invoice cannot move to that step.');
      return;
    }
    this.setInvoiceStatus(invoice.id, status);
  }

  failInvoice(invoice: InvoiceResponse) {
    if (!this.invoiceCanFail(invoice.invoiceStatus)) {
      this.showError('Only Processing invoices can fail.');
      return;
    }
    this.run(this.workspace.setInvoiceStatus(invoice.id, 'failed'), 'Invoice marked as failed.');
  }

  cancelInvoice(invoice: InvoiceResponse) {
    if (!this.invoiceCanCancel(invoice.invoiceStatus)) {
      this.showError('This invoice cannot be cancelled.');
      return;
    }
    this.run(this.workspace.setInvoiceStatus(invoice.id, 'cancelled'), 'Invoice cancelled.');
  }

  refundInvoice(invoice: InvoiceResponse) {
    if (!this.invoiceCanRefund(invoice.invoiceStatus)) {
      this.showError('Only Completed invoices can be refunded.');
      return;
    }
    this.run(this.workspace.setInvoiceStatus(invoice.id, 'refunded'), 'Invoice refunded.');
  }

  completedInvoicesCount() {
    return this.invoices.filter(invoice => invoice.invoiceStatus === 'completed').length;
  }

  percent(value: number, total: number) {
    return total > 0 ? Math.round((value / total) * 100) : 0;
  }

  maxValue<T extends object>(items: T[], key: keyof T = 'value' as keyof T) {
    const currentKey = key;

    return Math.max(...items.map(item => Number(item[currentKey]) || 0), 1);
  }

  openOrdersCount() {
    return (this.stats?.ordersCount ?? 0) - (this.stats?.ordersDelivered ?? 0) - (this.stats?.ordersCancelled ?? 0);
  }

  deliveryOrderOptions() {
    return this.orders.filter(order => order.status === 'validated' || order.status === 'ongoing');
  }

  invoiceOrderOptions() {
    return this.orders.filter(order => order.status === 'validated' || order.status === 'ongoing' || order.status === 'delivered');
  }

  orderChartBackground() {
    const total = this.stats?.ordersCount ?? 0;
    const pending = this.percent(this.stats?.ordersPendingApproval ?? 0, total);
    const ongoing = this.percent(this.stats?.ordersOngoing ?? 0, total);
    const delivered = this.percent(this.stats?.ordersDelivered ?? 0, total);
    const pendingEnd = pending;
    const ongoingEnd = pending + ongoing;
    const deliveredEnd = pending + ongoing + delivered;
    return `conic-gradient(#7a1823 0 ${pendingEnd}%, #a64b54 ${pendingEnd}% ${ongoingEnd}%, #e7e9ea ${ongoingEnd}% ${deliveredEnd}%, #2f3336 ${deliveredEnd}% 100%)`;
  }

  loadAll() {
    this.loading = true;
    this.error = '';
    this.getWorkspaceData()
      .pipe(finalize(() => {
        this.loading = false;
        this.updateView();
      }))
      .subscribe({
        next: response => {
          this.setWorkspaceData(response);
          this.updateView();
        },
        error: error => this.showError(this.readError(error))
      });
  }

  saveCustomer() {
    const request: CustomerRequest = {
      name: this.customerForm.name.trim(),
      email: this.customerForm.email.trim().toLowerCase(),
      address: this.customerForm.address?.trim(),
      phone: this.customerForm.phone?.trim()
    };
    if (!request.name || !request.email) {
      this.showError('Fill all required customer fields.');
      return;
    }
    if (!this.emailPattern.test(request.email)) {
      this.showError('Enter a valid email address.');
      return;
    }
    this.run(this.workspace.saveCustomer(request, this.customerEditId), 'Customer saved.', () => {
      this.customerForm = this.emptyCustomer();
      this.customerEditId = '';
      this.closeForm('customers');
    });
  }

  editCustomer(customer: CustomerResponse) {
    this.customerEditId = customer.id;
    this.customerForm = { name: customer.name, email: customer.email, address: customer.address, phone: customer.phone };
    this.formSections.customers = true;
  }

  deleteCustomer(id: string) {
    this.openConfirmation('Delete customer', 'This customer will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteCustomer(id), 'Customer deleted.'));
  }

  saveCarrier() {
    const request: CarrierRequest = {
      name: this.carrierForm.name.trim(),
      phone: this.carrierForm.phone?.trim(),
      rating: Number(this.carrierForm.rating ?? 0),
      isActive: this.carrierForm.isActive
    };
    if (!request.name) {
      this.showError('Fill all required carrier fields.');
      return;
    }
    if (Number.isNaN(request.rating) || Number(request.rating) < 0 || Number(request.rating) > 5) {
      this.showError('Carrier rating must be between 0 and 5.');
      return;
    }
    this.run(this.workspace.saveCarrier(request, this.carrierEditId), 'Carrier saved.', () => {
      this.carrierForm = this.emptyCarrier();
      this.carrierEditId = '';
      this.closeForm('carriers');
    });
  }

  editCarrier(carrier: CarrierResponse) {
    this.carrierEditId = carrier.id;
    this.carrierForm = { name: carrier.name, phone: carrier.phone, rating: carrier.rating ?? 0, isActive: carrier.isActive };
    this.formSections.carriers = true;
  }

  deleteCarrier(id: string) {
    this.openConfirmation('Delete carrier', 'This carrier will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteCarrier(id), 'Carrier deleted.'));
  }

  saveSupplier() {
    const request: SupplierRequest = {
      name: this.supplierForm.name.trim(),
      email: this.supplierForm.email?.trim().toLowerCase(),
      phone: this.supplierForm.phone?.trim(),
      address: this.supplierForm.address?.trim(),
      isActive: this.supplierForm.isActive
    };
    if (!request.name) {
      this.showError('Fill all required supplier fields.');
      return;
    }
    if (request.email && !this.emailPattern.test(request.email)) {
      this.showError('Enter a valid email address.');
      return;
    }
    this.run(this.workspace.saveSupplier(request, this.supplierEditId), 'Supplier saved.', () => {
      this.supplierForm = this.emptySupplier();
      this.supplierEditId = '';
      this.closeForm('suppliers');
    });
  }

  editSupplier(supplier: SupplierResponse) {
    this.supplierEditId = supplier.id;
    this.supplierForm = { name: supplier.name, email: supplier.email, phone: supplier.phone, address: supplier.address, isActive: supplier.isActive };
    this.formSections.suppliers = true;
  }

  deleteSupplier(id: string) {
    this.openConfirmation('Delete supplier', 'This supplier will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteSupplier(id), 'Supplier deleted.'));
  }

  saveStock() {
    const request: StockRequest = {
      product: this.stockForm.product.trim(),
      productRef: this.stockForm.productRef?.trim(),
      locationId: this.stockForm.locationId || undefined,
      quantity: Number(this.stockForm.quantity)
    };
    if (!request.product) {
      this.showError('Fill all required stock fields.');
      return;
    }
    if (Number.isNaN(request.quantity) || request.quantity < 0) {
      this.showError('Stock quantity must be zero or more.');
      return;
    }
    this.run(this.workspace.saveStock(request, this.stockEditId), 'Stock saved.', () => {
      this.stockForm = this.emptyStock();
      this.stockEditId = '';
      this.closeForm('stock');
    });
  }

  editStock(stock: StockResponse) {
    this.stockEditId = stock.id;
    this.stockForm = { product: stock.product, productRef: stock.productRef, locationId: stock.locationId, quantity: stock.quantity };
    this.formSections.stock = true;
  }

  deleteStock(id: string) {
    this.openConfirmation('Delete stock item', 'This stock item will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteStock(id), 'Stock deleted.'));
  }

  saveLocation() {
    const request: LocationRequest = {
      name: this.locationForm.name.trim(),
      code: this.locationForm.code?.trim(),
      description: this.locationForm.description?.trim()
    };
    if (!request.name) {
      this.showError('Fill all required location fields.');
      return;
    }
    this.run(this.workspace.saveLocation(request, this.locationEditId), 'Location saved.', () => {
      this.locationForm = this.emptyLocation();
      this.locationEditId = '';
      this.closeForm('locations');
    });
  }

  editLocation(location: LocationResponse) {
    this.locationEditId = location.id;
    this.locationForm = { name: location.name, code: location.code, description: location.description };
    this.formSections.locations = true;
  }

  deleteLocation(id: string) {
    this.openConfirmation('Delete location', 'This location will be removed and assigned stock items will be unassigned.', 'Delete', () => this.run(this.workspace.deleteLocation(id), 'Location deleted.'));
  }

  stockProductOptions() {
    const products = new Map<string, { product: string; productRef?: string; quantity: number; reservedQuantity: number; availableQuantity: number; refs: Set<string> }>();

    this.stocks.forEach(stock => {
      const productName = stock.product?.trim();
      if (!productName) {
        return;
      }
      const key = productName.toLowerCase();
      const current = products.get(key) ?? { product: productName, productRef: stock.productRef, quantity: 0, reservedQuantity: 0, availableQuantity: 0, refs: new Set<string>() };
      current.quantity += Number(stock.quantity ?? 0);
      current.reservedQuantity = Math.max(current.reservedQuantity, Number(stock.reservedQuantity ?? 0));
      current.availableQuantity = Math.max(current.availableQuantity, Number(stock.availableQuantity ?? 0));
      if (stock.productRef) {
        current.refs.add(stock.productRef);
      }
      products.set(key, current);
    });

    return [...products.values()]
      .map(product => ({
        product: product.product,
        productRef: product.refs.size === 1 ? [...product.refs][0] : product.productRef,
        quantity: this.availableOrderQuantity(product)
      }))
      .filter(product => product.quantity > 0)
      .sort((first, second) => first.product.localeCompare(second.product));
  }

  availableOrderQuantity(product: { product: string; quantity: number; reservedQuantity: number; availableQuantity: number }) {
    const availableQuantity = product.availableQuantity || Math.max(product.quantity - product.reservedQuantity, 0);
    if (!this.orderEditId) {
      return availableQuantity;
    }
    return availableQuantity + this.orderProductQuantity(product.product);
  }

  selectOrderProduct(productName: string) {
    const stock = this.stockProductOptions().find(product => product.product === productName);
    this.productForm.product = stock?.product ?? '';
    this.productForm.productRef = stock?.productRef;
  }

  selectedStockQuantity() {
    const productName = this.productForm.product.trim().toLowerCase();
    const stock = this.stockProductOptions().find(product => product.product.toLowerCase() === productName);

    return stock?.quantity ?? 0;
  }

  orderProductQuantity(productName: string) {
    const key = productName.trim().toLowerCase();

    return this.orderForm.products
      .filter(product => product.product.trim().toLowerCase() === key)
      .reduce((total, product) => total + Number(product.quantity ?? 0), 0);
  }

  addProduct() {
    if (!this.productForm.product.trim() || Number(this.productForm.quantity) < 1 || Number(this.productForm.pricePerUnit) <= 0) {
      this.showError('Fill valid product details.');
      return;
    }
    const stockQuantity = this.selectedStockQuantity();
    const requestedQuantity = this.orderProductQuantity(this.productForm.product) + Number(this.productForm.quantity);
    if (stockQuantity <= 0) {
      this.showError('Select a product from stock.');
      return;
    }
    if (requestedQuantity > stockQuantity) {
      this.showError('Requested quantity exceeds available stock.');
      return;
    }
    this.orderForm.products = [...this.orderForm.products, {
      product: this.productForm.product.trim(),
      productRef: this.productForm.productRef?.trim(),
      quantity: Number(this.productForm.quantity),
      pricePerUnit: Number(this.productForm.pricePerUnit)
    }];
    this.productForm = this.emptyProduct();
  }

  removeProduct(index: number) {
    this.orderForm.products = this.orderForm.products.filter((_, current) => current !== index);
  }

  orderProductsWithinStock() {
    return this.orderForm.products.every(product => {
      const stock = this.stockProductOptions().find(item => item.product.trim().toLowerCase() === product.product.trim().toLowerCase());
      return !!stock && this.orderProductQuantity(product.product) <= stock.quantity;
    });
  }

  saveOrder() {
    if (!this.orderForm.customerId || !this.orderForm.supplierId || !this.orderForm.products.length) {
      this.showError('Select a customer, select a supplier, and add at least one product.');
      return;
    }
    if (!this.orderProductsWithinStock()) {
      this.showError('Requested quantity exceeds available stock.');
      return;
    }
    const request: OrderRequest = {
      customerId: this.orderForm.customerId,
      supplierId: this.orderForm.supplierId,
      remark: this.orderForm.remark?.trim(),
      products: this.orderForm.products
    };
    this.run(this.workspace.saveOrder(request, this.orderEditId), 'Order saved.', () => {
      this.orderForm = this.emptyOrder();
      this.orderEditId = '';
      this.closeForm('orders');
    });
  }

  editOrder(order: OrderSummaryResponse) {
    this.workspace.getOrder(order.id).subscribe({
      next: response => {
        this.orderEditId = response.id;
        this.orderForm = {
          customerId: response.customer.id,
          supplierId: response.supplier?.id ?? '',
          remark: response.remark,
          products: response.products.map(product => ({
            product: product.product,
            productRef: product.productRef,
            quantity: product.quantity,
            pricePerUnit: product.pricePerUnit
          }))
        };
        this.setSection('orders');
        this.formSections.orders = true;
        this.updateView();
      },
      error: error => this.showError(this.readError(error))
    });
  }

  validateOrder(id: string) {
    this.run(this.workspace.validateOrder(id), 'Order validated.');
  }

  processOrder(order: OrderSummaryResponse, status: OrderStatus) {
    if (order.status === status) {
      return;
    }
    if (order.status === 'pendingApproval' && status === 'validated') {
      this.validateOrder(order.id);
      return;
    }
    if (order.status === 'validated' && status === 'ongoing') {
      this.openDeliveryForOrder(order);
      return;
    }
    if (status === 'delivered') {
      this.showError('Set a delivery as Delivered to complete this order.');
      return;
    }
    this.showError('This order cannot move to that step.');
  }

  cancelOrder(order: OrderSummaryResponse) {
    if (order.status !== 'pendingApproval' && order.status !== 'validated') {
      this.showError('Only Pending Approval or Validated orders can be cancelled.');
      return;
    }
    this.openConfirmation('Cancel Order', 'This order will be cancelled.', 'Cancel Order', () => this.run(this.workspace.setOrderStatus(order.id, 'cancelled'), 'Order cancelled.'));
  }

  setOrderStatus(id: string, status: OrderStatus) {
    this.run(this.workspace.setOrderStatus(id, status), 'Order status updated.');
  }

  deleteOrder(order: OrderSummaryResponse) {
    if (!this.orderCanDelete(order.status)) {
      this.showError('Only Pending Approval or Cancelled orders can be deleted.');
      return;
    }
    this.openConfirmation('Delete order', 'This order will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteOrder(order.id), 'Order deleted.'));
  }

  openDeliveryForOrder(order: OrderSummaryResponse) {
    this.shippingEditId = '';
    this.shippingForm = this.emptyShipping();
    this.shippingForm.orderId = order.id;
    this.setSection('deliveries');
    this.formSections.deliveries = true;
  }

  saveShipping() {
    const request: ShippingRequest = {
      orderId: this.shippingForm.orderId,
      carrierId: this.shippingForm.carrierId || undefined,
      deliveryDate: this.shippingForm.deliveryDate || undefined,
      cost: Number(this.shippingForm.cost ?? 0),
      shippingAddress: this.shippingForm.shippingAddress?.trim(),
      trackingNumber: this.shippingForm.trackingNumber?.trim(),
      remark: this.shippingForm.remark?.trim()
    };
    if (!request.orderId) {
      this.showError('Select an order.');
      return;
    }
    if (Number.isNaN(request.cost) || Number(request.cost) < 0) {
      this.showError('Delivery cost must be zero or more.');
      return;
    }
    this.run(this.workspace.saveShipping(request, this.shippingEditId), 'Delivery saved.', () => {
      this.shippingForm = this.emptyShipping();
      this.shippingEditId = '';
      this.closeForm('deliveries');
    });
  }

  editShipping(shipping: ShippingResponse) {
    this.shippingEditId = shipping.id;
    this.shippingForm = {
      orderId: shipping.orderId,
      carrierId: shipping.carrier?.id,
      deliveryDate: shipping.deliveryDate?.slice(0, 16),
      cost: shipping.cost ?? 0,
      shippingAddress: shipping.shippingAddress,
      trackingNumber: shipping.trackingNumber,
      remark: shipping.remark
    };
    this.formSections.deliveries = true;
  }

  setShippingStatus(id: string, status: ShippingStatus) {
    this.run(this.workspace.setShippingStatus(id, status), 'Delivery status updated.');
  }

  deleteShipping(id: string) {
    this.openConfirmation('Delete delivery', 'This delivery will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteShipping(id), 'Delivery deleted.'));
  }

  saveInvoice() {
    const request: InvoiceRequest = {
      orderId: this.invoiceForm.orderId,
      method: this.invoiceForm.method,
      amount: Number(this.invoiceForm.amount),
      transactionRef: this.invoiceForm.transactionRef?.trim(),
      remark: this.invoiceForm.remark?.trim()
    };
    if (!request.orderId) {
      this.showError('Select an order.');
      return;
    }
    if (Number.isNaN(request.amount) || request.amount <= 0) {
      this.showError('Invoice amount must be greater than zero.');
      return;
    }
    this.run(this.workspace.saveInvoice(request), 'Invoice saved.', () => {
      this.invoiceForm = this.emptyInvoice();
      this.closeForm('payments');
    });
  }

  setInvoiceStatus(id: string, status: InvoiceStatus) {
    this.run(this.workspace.setInvoiceStatus(id, status), 'Invoice status updated.');
  }

  deleteInvoice(id: string) {
    this.openConfirmation('Delete invoice', 'This invoice will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteInvoice(id), 'Invoice deleted.'));
  }

  filteredUsers() {
    const criteria = this.filters.users.trim().toLowerCase();

    return this.users.filter(user => !criteria
      || user.name.toLowerCase().includes(criteria)
      || user.username.toLowerCase().includes(criteria)
      || user.email.toLowerCase().includes(criteria)
      || this.displayLabel(user.role).toLowerCase().includes(criteria));
  }

  filteredSupportTickets() {
    const criteria = this.filters.support.trim().toLowerCase();
    const status = this.filters.supportStatus;

    return this.supportTickets.filter(ticket => {
      const matchesCriteria = !criteria
        || ticket.subject.toLowerCase().includes(criteria)
        || (ticket.requesterName ?? '').toLowerCase().includes(criteria)
        || (ticket.requesterEmail ?? '').toLowerCase().includes(criteria)
        || this.displayLabel(ticket.status).toLowerCase().includes(criteria);
      const matchesStatus = !status || ticket.status === status;

      return matchesCriteria && matchesStatus;
    });
  }

  supportAssignableUsers() {
    return this.users.filter(user => user.isActive && (user.role === 'administrator' || user.role === 'manager'));
  }

  activeSuppliers() {
    return this.suppliers.filter(supplier => supplier.isActive);
  }

  activeCarriers() {
    return this.carriers.filter(carrier => carrier.isActive);
  }

  saveUser() {
    const request: UserRequest = {
      username: this.userForm.username.trim(),
      name: this.userForm.name.trim(),
      email: this.userForm.email.trim().toLowerCase(),
      role: this.userForm.role,
      isActive: this.userForm.isActive,
      password: this.userForm.password?.trim() || undefined
    };
    if (!request.username || !request.name || !request.email) {
      this.showError('Fill all required user fields.');
      return;
    }
    if (!this.emailPattern.test(request.email)) {
      this.showError('Enter a valid email address.');
      return;
    }
    if (!this.userEditId && !request.password) {
      this.showError('Password must be filled.');
      return;
    }
    this.run(this.workspace.saveUser(request, this.userEditId), 'User saved.', () => {
      this.userForm = this.emptyUser();
      this.userEditId = '';
      this.closeForm('users');
    });
  }

  editUser(user: UserResponse) {
    this.userEditId = user.id;
    this.userForm = {
      username: user.username,
      name: user.name,
      email: user.email,
      role: user.role,
      isActive: user.isActive,
      password: ''
    };
    this.formSections.users = true;
  }

  deleteUser(user: UserResponse) {
    this.openConfirmation('Delete User', 'This user account will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteUser(user.id), 'User deleted.'));
  }

  submitSupportTicket() {
    if (this.supportEditId) {
      const request = this.supportStatusForm[this.supportEditId];
      if (!request) {
        this.showError('Select ticket status.');
        return;
      }
      this.run(this.workspace.updateSupportTicket(this.supportEditId, {
        status: request.status,
        assignedUserId: request.assignedUserId || undefined
      }), 'Ticket updated.', () => {
        this.supportEditId = '';
        this.closeForm('support');
      });
      return;
    }
    if (!this.supportForm.subject.trim() || !this.supportForm.description.trim()) {
      this.showError('Fill the subject and description.');
      return;
    }
    const request: SupportTicketRequest = {
      subject: this.supportForm.subject.trim(),
      description: this.supportForm.description.trim(),
      category: this.supportForm.category,
      priority: this.supportForm.priority,
      assignedUserId: this.supportForm.assignedUserId || undefined
    };
    this.run(this.workspace.saveSupportTicket(request), 'Ticket created.', () => {
      this.supportForm = this.emptySupport();
      this.closeForm('support');
    });
  }

  editSupportTicket(ticket: SupportTicketResponse) {
    this.supportEditId = ticket.id;
    this.supportStatusForm[ticket.id] = {
      status: ticket.status,
      assignedUserId: ticket.assignedUserId ?? ''
    };
    this.formSections.support = true;
  }

  updateSupportTicket(ticket: SupportTicketResponse) {
    const request = this.supportStatusForm[ticket.id] ?? { status: ticket.status, assignedUserId: ticket.assignedUserId ?? '' };
    this.run(this.workspace.updateSupportTicket(ticket.id, {
      status: request.status,
      assignedUserId: request.assignedUserId || undefined
    }), 'Ticket updated.');
  }

  deleteSupportTicket(ticket: SupportTicketResponse) {
    this.openConfirmation('Delete Ticket', 'This support ticket will be removed permanently.', 'Delete', () => this.run(this.workspace.deleteSupportTicket(ticket.id), 'Ticket deleted.'));
  }

  closeConfirmation() {
    this.confirmation = null;
  }

  confirmAction() {
    const action = this.confirmation?.action;
    this.confirmation = null;
    action?.();
  }

  private run(action: Observable<unknown>, message: string, done?: () => void) {
    this.loading = true;
    this.clearMessages();
    action.pipe(
      timeout(this.requestTimeout),
      switchMap(() => this.getWorkspaceData()),
      finalize(() => {
        this.loading = false;
        this.updateView();
      })
    ).subscribe({
      next: response => {
        this.setWorkspaceData(response);
        done?.();
        this.showSuccess(message);
        this.updateView();
      },
      error: error => this.showError(this.readError(error))
    });
  }

  private getWorkspaceData(): Observable<WorkspaceData> {
    return forkJoin({
      stats: this.workspace.getStats().pipe(timeout(this.requestTimeout), catchError(() => of(this.stats ?? this.emptyStats()))),
      customers: this.workspace.getCustomers(this.filters.customers).pipe(timeout(this.requestTimeout), catchError(() => of(this.customers))),
      carriers: this.workspace.getCarriers().pipe(timeout(this.requestTimeout), catchError(() => of(this.carriers))),
      suppliers: this.workspace.getSuppliers(this.filters.suppliers).pipe(timeout(this.requestTimeout), catchError(() => of(this.suppliers))),
      stocks: this.workspace.getStocks(this.filters.stock).pipe(timeout(this.requestTimeout), catchError(() => of(this.stocks))),
      locations: this.workspace.getLocations(this.filters.locations).pipe(timeout(this.requestTimeout), catchError(() => of(this.locations))),
      orders: this.workspace.getOrders(this.selectedOrderStatus()).pipe(timeout(this.requestTimeout), catchError(() => of(this.currentOrdersPage()))),
      shippings: this.workspace.getShippings(this.selectedShippingStatus()).pipe(timeout(this.requestTimeout), catchError(() => of(this.shippings))),
      invoices: this.workspace.getInvoices().pipe(timeout(this.requestTimeout), catchError(() => of(this.invoices))),
      users: this.isAdmin() ? this.workspace.getUsers().pipe(timeout(this.requestTimeout), catchError(() => of(this.users))) : of(this.users),
      supportTickets: this.isAdmin() ? this.workspace.getSupportTickets(this.filters.support).pipe(timeout(this.requestTimeout), catchError(() => of(this.supportTickets))) : of(this.supportTickets)
    });
  }

  private setWorkspaceData(response: WorkspaceData) {
    this.stats = response.stats;
    this.customers = response.customers;
    this.carriers = response.carriers;
    this.suppliers = response.suppliers;
    this.stocks = response.stocks;
    this.locations = response.locations;
    this.orders = response.orders.content;
    this.shippings = response.shippings;
    this.invoices = response.invoices;
    this.users = response.users;
    this.supportTickets = response.supportTickets;
    this.supportTickets.forEach(ticket => {
      this.supportStatusForm[ticket.id] = this.supportStatusForm[ticket.id] ?? { status: ticket.status, assignedUserId: ticket.assignedUserId ?? '' };
    });
  }

  private restoreActiveSection() {
    const stored = localStorage.getItem(this.activeSectionKey) as Section | null;

    if (stored && this.isSection(stored)) {
      this.activeSection = stored;
    }
  }

  private ensureAllowedSection() {
    if (!this.canAccessSection(this.activeSection)) {
      this.activeSection = this.defaultSection();
      this.saveActiveSection(this.activeSection);
    }
  }

  private saveActiveSection(section: Section) {
    localStorage.setItem(this.activeSectionKey, section);
  }

  private restoreProfileIcon() {
    const icon = localStorage.getItem(this.profileIconKey);

    if (icon && this.profileIcons.includes(icon)) {
      this.profileIcon = icon;
    }
  }

  private isSection(section: string): section is Section {
    return this.sections.some(item => item.id === section) || section === 'profile';
  }

  private enumLabel(value: string) {
    return value
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/[_-]+/g, ' ')
      .split(' ')
      .filter(Boolean)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }

  private emptyStats(): DashboardStats {
    return {
      ordersCount: 0,
      ordersPendingApproval: 0,
      ordersOngoing: 0,
      ordersDelivered: 0,
      ordersCancelled: 0,
      revenue: 0,
      totalCustomers: 0,
      totalCarriers: 0,
      totalSuppliers: 0,
      totalStocks: 0,
      shippingInPerparation: 0,
      invoicePending: 0
    };
  }

  private currentOrdersPage(): PageResponse<OrderSummaryResponse> {
    return {
      content: this.orders,
      totalElements: this.orders.length,
      totalPages: 1,
      number: 0,
      size: this.orders.length
    };
  }

  private selectedOrderStatus(): OrderStatus | undefined {
    const status = this.filters.orderStatus as OrderStatus;
    return this.orderStatuses.includes(status) ? status : undefined;
  }

  private selectedShippingStatus(): ShippingStatus | undefined {
    const status = this.filters.shippingStatus as ShippingStatus;
    return this.shippingStatuses.includes(status) ? status : undefined;
  }

  dismissToast() {
    this.toast = null;
    this.error = '';
    this.success = '';
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = undefined;
    }
    this.updateView();
  }

  private emptyCustomer(): CustomerRequest {
    return { name: '', email: '', address: '', phone: '' };
  }

  private emptyCarrier(): CarrierRequest {
    return { name: '', phone: '', rating: 0, isActive: true };
  }

  private emptySupplier(): SupplierRequest {
    return { name: '', email: '', phone: '', address: '', isActive: true };
  }

  private emptyStock(): StockRequest {
    return { product: '', productRef: '', locationId: '', quantity: 0 };
  }

  private emptyLocation(): LocationRequest {
    return { name: '', code: '', description: '' };
  }

  private emptyProduct(): ProductRequest {
    return { product: '', productRef: '', quantity: 1, pricePerUnit: 0 };
  }

  private emptyOrder(): OrderRequest {
    return { customerId: '', supplierId: '', remark: '', products: [] };
  }

  private emptyShipping(): ShippingRequest {
    return { orderId: '', carrierId: '', deliveryDate: '', cost: 0, shippingAddress: '', trackingNumber: '', remark: '' };
  }

  private emptyInvoice(): InvoiceRequest {
    return { orderId: '', method: 'Cash', amount: 0, transactionRef: '', remark: '' };
  }

  private emptyUser(): UserRequest {
    return { username: '', name: '', email: '', role: 'user', isActive: true, password: '' };
  }

  private emptyProfile(): ProfileRequest {
    return { username: '', name: '', email: '', password: '' };
  }

  private emptySupport() {
    return { subject: '', category: 'operations' as SupportCategory, priority: 'normal' as SupportPriority, description: '', assignedUserId: '' };
  }

  private setProfileForm() {
    const user = this.auth.user();
    this.profileForm = { username: user?.username ?? '', name: user?.name ?? '', email: user?.email ?? '', password: '' };
  }

  private openConfirmation(title: string, message: string, confirmText: string, action: () => void) {
    this.confirmation = { title, message, confirmText, action };
  }

  private clearMessages() {
    this.error = '';
    this.success = '';
    this.toast = null;
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = undefined;
    }
    this.updateView();
  }

  private showSuccess(message: string) {
    this.success = message;
    this.error = '';
    this.showToast('success', message);
  }

  private showError(message: string) {
    this.error = message;
    this.success = '';
    this.showToast('error', message);
  }

  private showToast(type: 'success' | 'error', message: string) {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
    this.toast = { type, message };
    this.updateView();
    this.toastTimer = setTimeout(() => {
      this.dismissToast();
    }, 3000);
  }

  private readError(error: unknown) {
    const response = error as { name?: string; message?: string; error?: string | { message?: string; detail?: string; title?: string; errors?: Record<string, string> } };
    if (response.name === 'TimeoutError') {
      return 'Request timed out. Please try again.';
    }
    if (typeof response.error === 'string') {
      return response.error || response.message || 'Operation failed.';
    }
    const fieldError = response.error?.errors ? Object.values(response.error.errors).find(Boolean) : '';
    return fieldError || response.error?.message || response.error?.detail || response.error?.title || response.message || 'Operation failed.';
  }

  private updateView() {
    this.cdr.markForCheck();
  }
}
