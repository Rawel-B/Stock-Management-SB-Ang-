# Delivery Stock Management

Delivery Stock Management is a web application for managing orders, deliveries, stock, customers, suppliers, carriers, invoices, users, and support tickets.

The project is built with a separated architecture:

- Spring Boot REST API backend
- Angular frontend
- MongoDB database
- Docker-based deployment support

## Features

- JWT authentication
- Sign in, sign up, forgot password
- Remember me support
- Role-based access control
- Admin user management
- Account locking
- Order management
- Order validation workflow
- Delivery tracking workflow
- Invoice management
- Customer management
- Supplier management
- Carrier management
- Stock management
- Location management
- Support ticket system
- Dashboard with KPIs, slicers, and charts

## Roles

### Administrator

- Full application access
- User management
- Support ticket management
- Account locking
- Dashboard access
- Operational modules access

### Manager

- Operational modules access
- Orders, deliveries, invoices, customers, suppliers, carriers, stock, and locations
- No user management
- No support administration

### User

- Limited operational access
- No dashboard administration
- No user management
- No support administration

## Main Modules

- Dashboard
- Orders
- Deliveries
- Invoices
- Customers
- Suppliers
- Carriers
- Stock
- Locations
- Users
- Support Tickets
- Profile

## Business Flow

1. Customers and suppliers are created.
2. Stock items are registered and assigned to locations.
3. Orders are created with one or more products.
4. Orders are validated before processing.
5. Deliveries are created from validated orders.
6. Delivery status updates track the fulfillment process.
7. Delivered orders update stock reception.
8. Invoices are created and tracked by status.
9. Dashboard metrics summarize orders, deliveries, revenue, stock, and workflow status.

## Backend

### Stack

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data MongoDB
- Spring Validation
- Swagger/OpenAPI
- Maven

### Run Backend Locally

```bash
cd backend
mvn spring-boot:run
```

Default backend URL:

```txt
http://localhost:8080
```

### Build Backend

```bash
cd backend
mvn clean package
```

### Swagger

After starting the backend, API documentation is available at:

```txt
http://localhost:8080/swagger-ui.html
```

or:

```txt
http://localhost:8080/swagger-ui/index.html
```

## Frontend

### Stack

- Angular
- TypeScript
- SCSS
- JWT bearer authentication
- Role-based interface visibility

### Run Frontend Locally

```bash
cd frontend
npm install
npm start
```

Default frontend URL:

```txt
http://localhost:4200
```

### Build Frontend

```bash
cd frontend
npm run build
```

## Docker

Run the full application with Docker Compose:

```bash
docker compose up --build
```

Stop containers:

```bash
docker compose down
```

## Security

- JWT is used for authenticated requests.
- Protected API requests require a bearer token.
- Disabled users cannot sign in.
- Existing tokens are rejected when the user account is locked.
- Admin-only endpoints are protected in the backend.
- Frontend navigation is filtered by role.

## Support Tickets

Administrators can create, assign, update, and delete support tickets.

Users who cannot sign in or recover their password can open an access ticket from:

- Sign in page
- Forgot password page

Tickets can be assigned to active administrators or managers.

## Validation

Backend validation is handled with Spring Validator annotations.

Validation includes:

- Required fields
- Email format
- Minimum and maximum sizes
- Positive numeric values
- Enum-based status values

Frontend forms also validate important fields before sending requests.

## Useful Commands

### Backend Compile

```bash
cd backend
mvn -q -DskipTests compile
```

### Frontend Build

```bash
cd frontend
npm run build
```

### Docker Run

```bash
docker compose up --build
```

## Default Development URLs

```txt
Frontend: http://localhost:4200
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
MongoDB:  mongodb://localhost:27017
```

## Requirements Covered

- Spring Boot backend
- Angular frontend
- REST API separation
- CRUD features
- Order validation
- Delivery tracking
- Stock and location management
- Customer and supplier management
- Invoice management
- Authentication and roles
- Swagger/OpenAPI documentation
- Spring validation
- Docker support
- Modular project structure
