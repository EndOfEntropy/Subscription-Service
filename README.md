# Subscription Service

A production-ready subscription management system built with Spring Boot and Stripe integration.

## Features

- 🔐 JWT Authentication
- 💳 Stripe Payment Integration
- 📧 Automated Invoice Generation (PDF)
- 🔔 Webhook Event Processing
- 👥 Role-Based Access Control (User/Admin)
- 📊 Admin Dashboard with Revenue Analytics
- 🧪 Comprehensive Testing (70%+ coverage)
- 📝 API Documentation (Swagger/OpenAPI)

## Tech Stack

- **Backend:** Spring Boot 3.2, Java 17
- **Database:** PostgreSQL
- **Payment:** Stripe API
- **Security:** Spring Security, JWT
- **Testing:** JUnit 5, Mockito, TestContainers
- **Documentation:** Swagger/OpenAPI
- **Deployment:** Heroku

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Stripe Account (test mode)

### Local Setup

1. **Clone repository:**
```bash
   git clone https://github.com/yourusername/subscription-service.git
   cd subscription-service
```

2. **Configure database:**
```bash
   createdb subscription_db
```

3. **Set environment variables:**
```bash
   export STRIPE_API_KEY=sk_test_your_key
   export STRIPE_WEBHOOK_SECRET=whsec_your_secret
```

4. **Run application:**
```bash
   mvn spring-boot:run
```

5. **Access Swagger UI:**
```
   http://localhost:8080/swagger-ui/index.html
```

### Testing Webhooks Locally
```bash
# Terminal 1: Start Stripe webhook forwarding
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Terminal 2: Run application
mvn spring-boot:run

# Terminal 3: Test payments
stripe payment_intents confirm pi_xxx --payment-method pm_card_visa
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login

### Subscriptions
- `GET /api/plans` - List subscription plans
- `POST /api/subscriptions` - Create subscription
- `GET /api/subscriptions/current` - Get current subscription
- `POST /api/subscriptions/cancel` - Cancel subscription

### Payments & Invoices
- `GET /api/payments/history` - Payment history
- `GET /api/invoices` - List invoices
- `GET /api/invoices/{id}/download` - Download PDF

### Admin (Requires ADMIN role)
- `GET /api/admin/users` - List all users
- `GET /api/admin/revenue` - Revenue statistics
- `GET /api/admin/payments` - All payments
- `PUT /api/admin/users/{id}/role` - Update user role

## Running Tests
```bash
# Unit + Integration tests
mvn test

# With coverage
mvn clean test jacoco:report
```

## Deployment

Deployed on Heroku: https://your-app.herokuapp.com

## Database Schema
```
users (id, email, password, role, stripe_customer_id)
subscription_plans (id, name, price, billing_cycle, stripe_price_id)
subscriptions (id, user_id, plan_id, status, stripe_subscription_id, ...)
payments (id, user_id, subscription_id, amount, status, ...)
invoices (id, user_id, payment_id, invoice_number, ...)
```

## Architecture
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
┌──────▼──────────────────────────┐
│   Spring Boot Application       │
│  ┌──────────────────────────┐  │
│  │    Controllers           │  │
│  │  - Auth, Subscription,   │  │
│  │    Payment, Admin        │  │
│  └────────┬─────────────────┘  │
│           │                     │
│  ┌────────▼─────────────────┐  │
│  │    Services              │  │
│  │  - Business Logic        │  │
│  └────────┬─────────────────┘  │
│           │                     │
│  ┌────────▼─────────────────┐  │
│  │    Repositories          │  │
│  │  - Data Access           │  │
│  └────────┬─────────────────┘  │
└───────────┼─────────────────────┘
            │
    ┌───────▼────────┐
    │   PostgreSQL   │
    └────────────────┘

External Services:
- Stripe API (Payments)
- Stripe Webhooks (Events)
```

## License

MIT

## Author

Mickael Grivolat - [View Source Code](https://github.com/EndOfEntropy/Subscription-Service) - [LinkedIn](https://www.linkedin.com/in/micka%C3%ABlgrivolat)
