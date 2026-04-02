# Backend - Fuel Voucher

## Prerequisites
- Java 17
- Maven 3.9+

## Setup
mvn clean install

## Run
mvn spring-boot:run

## Database
- Default profile uses local MySQL with:
	- URL: jdbc:mysql://localhost:3306/fuel_voucher
	- Username: root
	- Password: (empty)
- You can override with:
	- SPRING_DATASOURCE_URL
	- SPRING_DATASOURCE_USERNAME
	- SPRING_DATASOURCE_PASSWORD

## API docs
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health endpoint: http://localhost:8080/api/health

## Authentication
- Register: POST /api/auth/register
- Login: POST /api/auth/login
- Current user (JWT required): GET /api/auth/me

Example register payload:
{
	"name": "Mongi Thameur",
	"email": "mongi@example.com",
	"password": "StrongPass123"
}

Example login payload:
{
	"email": "mongi@example.com",
	"password": "StrongPass123"
}

## Bootstrap admin
On startup, an admin account is auto-created by default:
- Email: admin@wtm.local
- Password: Admin@12345

You can disable this behavior with APP_BOOTSTRAP_ADMIN_ENABLED=false.
