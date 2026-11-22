# Hospital Management System (HMS)

A backend application developed using Spring Boot, Spring Data JPA, MySQL and Spring Security with JWT authentication. It provides REST APIs to manage hospital operations, patient records, appointments, prescriptions, laboratory services, pharmacy, billing and reports.

## 1. Technologies Used

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- JWT Authentication
- MySQL 8
- Maven
- REST API
- Swagger / OpenAPI
- Postman
- STS (Spring Tool Suite)

## 2. Project Features

### Authentication and Authorization
- User login with email and password.
- JWT-based authentication.
- Role-based access control.
- BCrypt password encryption.

### User and Role Management
- User management.
- Role management.
- Accountant account creation.
- Multiple user roles.

### Hospital Management Modules
- Department Management
- Doctor Management
- Patient Management
- Appointment Management
- Medical Records Management
- Prescription Management
- Laboratory Management
- Pharmacy and Medicine Management
- Billing and Invoice Management
- Payment Management
- Dashboard and Reports

## 3. User Roles

The system supports the following roles:

1. ADMIN
2. DOCTOR
3. PATIENT
4. RECEPTIONIST
5. LAB_TECHNICIAN
6. PHARMACIST
7. ACCOUNTANT

Each role has access according to the configured authorization rules.

## 4. Project Configuration

### Requirements

- Java Development Kit
- MySQL Server 8
- Spring Tool Suite or compatible IDE
- Maven

### Database Setup

Create the database in MySQL:
DB_URL=jdbc:mysql://localhost:3306/hospital_management_db
DB_USERNAME=root
DB_PASSWORD=your_database_password

JWT_SECRET=your_base64_encoded_secret

LAB_TECHNICIAN_PASSWORD=your_lab_password
PHARMACIST_PASSWORD=your_pharmacist_password
RECEPTIONIST_PASSWORD=your_receptionist_password

## 5. Running the Application

1. Clone or download the project.
2. Open the project in Spring Tool Suite.
3. Configure MySQL database credentials.
4. Update Maven dependencies.
5. Run `HospitalBackendApplication.java`.
6. The backend server will start on port 8081.

Base URL:

```text
http://localhost:8081
```

## 6. Authentication

Login endpoint:

```http
POST /api/auth/login
```

Example request:

```json
{
  "email": "admin@example.com",
  "password": "YOUR_PASSWORD"
}
```

The login API returns a JWT token.

Use the token in protected API requests:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

## 7. Swagger API Documentation

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

OpenAPI documentation:

```text
http://localhost:8081/v3/api-docs
```

Swagger provides API documentation and an interface for testing REST endpoints with JWT authentication.

## 8. Important API Endpoints

| Module | Endpoint |
|---|---|
| Authentication | `/api/auth/login` |
| Users | `/api/users` |
| Doctors | `/api/doctors` |
| Patients | `/api/patients` |
| Appointments | `/api/appointments` |
| Invoices | `/api/invoices` |
| Payments | `/api/payments` |
| Dashboard | `/api/dashboard` |
| Reports | `/api/reports/summary` |

Other modules include Medical Records, Prescriptions, Laboratory and Pharmacy.

## 9. Security Features

- JWT-based stateless authentication.
- BCrypt password hashing.
- Role-based endpoint authorization.
- Protected REST APIs.
- Unauthorized requests return HTTP 401.
- Access-denied requests return HTTP 403.

## 10. Testing

The backend APIs have been tested using Postman and Swagger UI.

Testing includes:

- Login and JWT authentication.
- Role-based access control.
- CRUD operations.
- Appointment management.
- Prescription and laboratory workflows.
- Medicine dispensing and stock validation.
- Invoice and payment workflows.
- Dashboard and report APIs.
- Unauthorized and forbidden access testing.

## 11. Project Status

**Backend Development: Completed**

The backend includes all major hospital management modules, authentication, authorization, billing, payment processing, dashboard, reports and Swagger API documentation.

Frontend development and complete frontend-backend integration are the next development phases.
