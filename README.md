# JobBoard API

A production-ready REST API for a job board platform built with Spring Boot 3, featuring JWT authentication, OAuth2 social login, and role-based access control.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6, JWT (JJWT 0.12) |
| OAuth2 | Google, GitHub (via Spring OAuth2 Client) |
| Database | PostgreSQL 18 |
| ORM | Hibernate / Spring Data JPA |
| Validation | Jakarta Validation |
| Utilities | Lombok |
| Build Tool | Maven |
| API Testing | Postman |

---

## Features

- **Local Auth** — Register and login with email, password, and role
- **Password Security** — BCrypt hashing, raw password never stored
- **JWT Tokens** — Stateless authentication with signed, expiring tokens
- **OAuth2 Login** — Sign in with Google or GitHub
- **Role-Based Access** — `EMPLOYER` and `CANDIDATE` roles with separate permissions
- **Profile Completion Flow** — OAuth2 users without a role are redirected to complete their profile
- **Auto Schema Generation** — Hibernate creates and updates tables from `@Entity` classes
- **Connection Pooling** — HikariCP manages database connections

---

## Project Structure

```
src/main/java/com/jobboard/
├── controller/
├── dto/
│   ├── AuthResponse.java
|   |   ApiResponse.java
|   |   JobResponse.java
|   |   JobRequest.java
│   ├── LoginRequest.java
│   └── RegisterRequest.java
├── entity/
│   ├── Application.java
│   ├── Job.java
│   ├── Provider.java (enum)
│   ├── Role.java (enum)
│   └── User.java
├── exception/
│   ├── EmailAlreadyExistsException.java
│   ├── InvalidCredentialsException.java
│   └── UserNotFoundException.java
|   |   JobNotFoundException.java
├── repository/
│   ├── ApplicationRepository.java
│   ├── JobRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomOAuth2User.java
│   ├── CustomOAuth2UserService.java
│   ├── JwtFilter.java
│   ├── JwtService.java
│   ├── OAuth2AuthenticationSuccessHandler.java
│   └── SecurityConfig.java
├── service/
│   └── AuthService.java
└── JobboardApiApplication.java
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 18 running on port `5433`

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/jobboard-api.git
cd jobboard-api
```

### 2. Create the database

Connect to your PostgreSQL instance and run:

```sql
CREATE DATABASE jobboard;
```

### 3. Configure environment

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/jobboard
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your-very-long-secret-key-at-least-32-characters
jwt.expiration=86400000

# OAuth2 — Google
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile

# OAuth2 — GitHub
spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET
spring.security.oauth2.client.registration.github.scope=user:email
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Hibernate will auto-create all tables on first run. The API will be live at `http://localhost:8080`.

---

## API Reference

### Auth — Public

| Method | Endpoint | Description | Body |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | `email`, `password`, `role` |
| `POST` | `/auth/login` | Login and receive JWT | `email`, `password` |
| `GET` | `/oauth2/authorize/google` | Start Google OAuth2 flow | — |
| `GET` | `/oauth2/authorize/github` | Start GitHub OAuth2 flow | — |

### Jobs

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| `GET` | `/jobs` | Public | Browse all open jobs |
| `POST` | `/jobs` | `EMPLOYER` | Post a new job |
| `PUT` | `/jobs/{id}` | `EMPLOYER` | Update a job listing |
| `DELETE` | `/jobs/{id}` | `EMPLOYER` | Remove a job listing |
| `GET` | `/jobs/{id}/applications` | `EMPLOYER` | View applications for a job |

### Applications

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| `POST` | `/jobs/{id}/apply` | `CANDIDATE` | Apply to a job |
| `GET` | `/applications/mine` | `CANDIDATE` | View your applications |
| `DELETE` | `/applications/{id}` | `CANDIDATE` | Withdraw an application |

### Profile

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/profile` | Any authenticated user | View your profile |

---

## Authentication Flow

### Local Auth

```
POST /auth/register  →  BCrypt hash password  →  save User  →  return success
POST /auth/login     →  verify password        →  generate JWT  →  return token
```

All protected endpoints require the token in the request header:
```
Authorization: Bearer <your_jwt_token>
```

### OAuth2 Flow

```
GET /oauth2/authorize/google
  → Google consent screen
  → Spring exchanges code for user info
  → CustomOAuth2UserService finds or creates User
  → If role not set → redirect to /complete-profile
  → If role set     → generate JWT → redirect with token
```

---

## Security Architecture

Every request passes through this chain:

```
Request
  → JwtFilter          (validates Bearer token, sets SecurityContext)
  → SecurityConfig     (checks role against route rules)
  → Controller         (handles business logic)
  → Service / Repository
  → PostgreSQL
```

### Route Authorization Summary

| Route Pattern | Access |
|---|---|
| `POST /auth/**` | Public |
| `GET /oauth2/**` | Public |
| `GET /jobs` | Public |
| `POST/PUT/DELETE /jobs/**` | `EMPLOYER` only |
| `GET /jobs/{id}/applications` | `EMPLOYER` only |
| `POST /jobs/{id}/apply` | `CANDIDATE` only |
| `GET /applications/mine` | `CANDIDATE` only |
| `GET /profile` | Any authenticated user |

---

## Database Schema

```
users
  id (UUID, PK)
  email (unique)
  password (BCrypt hash, nullable for OAuth users)
  role (EMPLOYER | CANDIDATE, nullable until profile complete)
  provider (LOCAL | GOOGLE | GITHUB)
  provider_id
  profile_complete (boolean)
  created_at

job
  id (UUID, PK)
  title
  description
  location
  salary
  status (OPEN | CLOSED)
  email
  employer_id (FK → users)
  created_at

application
  id (UUID, PK)
  status (PENDING | REVIEWED | REJECTED | ACCEPTED)
  cv_filepath
  candidate_id (FK → users)
  job_id (FK → job)
  applied_at
  created_at
```

---

## Roadmap

- [ ] Job controller — CRUD endpoints
- [ ] Application controller — apply, view, withdraw
- [ ] Profile controller
- [ ] Complete-profile endpoint for OAuth2 users
- [ ] File upload for CVs
- [ ] Email notifications on application status change
- [ ] Pagination on job listings
- [ ] Search and filter jobs by location, salary, title
- [ ] Admin role and dashboard
- [ ] Docker + docker-compose setup
- [ ] Deploy to Railway / Render

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

## License

[MIT](LICENSE)

---

> Built with focus, one endpoint at a time.