# TalentSync — Smart Resource Allocation System (SRAS)

TalentSync is a full-stack web application that intelligently matches employees to project requirements using a weighted scoring algorithm. It provides role-based dashboards for **Project Managers** and **Employees**, enabling efficient workforce planning and assignment management.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Features](#features)
6. [Matching Algorithm](#matching-algorithm)
7. [API Reference](#api-reference)
8. [Data Models](#data-models)
9. [Frontend Pages & Routes](#frontend-pages--routes)
10. [Authentication & Authorization](#authentication--authorization)
11. [Getting Started](#getting-started)
12. [Environment Configuration](#environment-configuration)
13. [Running Tests](#running-tests)

---

## Overview

SRAS solves the problem of manually finding the right employee for a project role. A Project Manager defines a project with its required roles, skills, certifications, and experience levels. The system then scores every available employee against those requirements and presents a ranked shortlist. The manager can then assign employees directly from within the app.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser (Angular 19)                     │
│  Home · Login · Signup · Employee Form · Employee Dashboard     │
│  Manager Dashboard · Project Requirements · Matching Results    │
│  Ranking Dashboard · Forgot/Reset Password                      │
└─────────────────────┬───────────────────────────────────────────┘
                      │  HTTP/REST (JWT Bearer Token)
                      │  Proxy: localhost:4200 → localhost:8081
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot 4 REST API (port 8081)             │
│  AuthController · EmployeeController · ProjectController        │
│  MatchingController · AssignmentController                      │
│                                                                 │
│  JwtAuthFilter → SecurityConfig (role-based access)            │
└─────────────────────┬───────────────────────────────────────────┘
                      │  JPA / Hibernate
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                     MySQL Database (sras_db)                    │
│  users · employees · projects · roles · skills · certifications │
│  project_requirements · project_assignments                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## Tech Stack

### Backend (`sras-backend`)
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 4.0.6 | Application framework |
| Spring Security | (Boot managed) | Authentication & authorization |
| Spring Data JPA / Hibernate | (Boot managed) | ORM & database access |
| MySQL Connector/J | (Boot managed) | Database driver |
| JJWT | 0.12.6 | JWT token generation & validation |
| Lombok | (Boot managed) | Boilerplate reduction |
| Spring Validation | (Boot managed) | Request validation |
| Maven | 3.x | Build tool |

### Frontend (`sras-ui`)
| Technology | Version | Purpose |
|---|---|---|
| Angular | 19.2 | SPA framework |
| Angular Material | 19.2 | UI component library |
| Angular CDK | 19.2 | Component Dev Kit |
| RxJS | 7.8 | Reactive programming |
| TypeScript | 5.6 | Language |
| SCSS | — | Styling |

---

## Project Structure

```
TalentSync/
├── sras-backend/                   # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/pss/SRAS/
│       ├── SrasApplication.java    # Entry point
│       ├── config/                 # CORS & MVC configuration
│       │   ├── CorsConfig.java
│       │   └── WebMvcConfig.java
│       ├── controllers/            # REST controllers
│       │   ├── AuthController.java
│       │   ├── EmployeeController.java
│       │   ├── ProjectController.java
│       │   ├── MatchingController.java
│       │   └── AssignmentController.java
│       ├── dto/                    # Data Transfer Objects
│       │   ├── LoginRequest.java
│       │   ├── SignupRequest.java
│       │   ├── AuthResponse.java
│       │   ├── ForgotPasswordRequest.java
│       │   ├── ResetPasswordRequest.java
│       │   ├── AssignmentRequest.java
│       │   ├── AssignmentResponseDto.java
│       │   ├── MatchingResultDto.java
│       │   ├── ProjectDashboardDto.java
│       │   ├── EmployeeProjectDto.java
│       │   └── ApiError.java
│       ├── models/                 # JPA entities
│       │   ├── User.java
│       │   ├── Employee.java
│       │   ├── Project.java
│       │   ├── ProjectRequirement.java
│       │   ├── ProjectAssignment.java
│       │   ├── Role.java
│       │   ├── Skill.java
│       │   ├── Certification.java
│       │   └── enums/
│       │       ├── AvailabilityStatus.java
│       │       └── ExperienceLevel.java
│       ├── repositories/           # Spring Data repositories
│       ├── services/               # Business logic
│       │   ├── AuthService.java
│       │   ├── EmployeeService.java
│       │   ├── ProjectService.java
│       │   ├── MatchingService.java
│       │   └── AssignmentService.java
│       ├── security/               # JWT & Spring Security
│       │   ├── JwtUtil.java
│       │   ├── JwtAuthFilter.java
│       │   ├── SecurityConfig.java
│       │   └── UserDetailsServiceImpl.java
│       └── exception/              # Global exception handling
│
└── sras-ui/                        # Angular application
    ├── angular.json
    ├── package.json
    ├── proxy.conf.json             # Proxies /api → :8081
    └── src/app/
        ├── app.routes.ts           # Lazy-loaded route definitions
        ├── app.config.ts
        ├── components/
        │   ├── home/               # Landing page
        │   ├── login/              # Login form
        │   ├── signup/             # Registration form
        │   ├── forgot-password/    # Forgot-password flow
        │   ├── reset-password/     # Reset-password flow
        │   ├── employee-form/      # Create/edit employee profile
        │   ├── employee-dashboard/ # Employee: view assigned projects
        │   ├── ranking-dashboard/  # Employee-visible ranking view
        │   ├── project-requirement/# Manager: manage projects & requirements
        │   ├── matching-results/   # Manager: view ranked employee matches
        │   └── manager-dashboard/  # Manager: overview of all assignments
        ├── guards/
        │   └── auth.guard.ts       # authGuard + managerGuard
        ├── interceptors/
        │   └── auth.interceptor.ts # Attaches JWT to every request
        ├── models/                 # TypeScript interfaces
        │   ├── auth.model.ts
        │   ├── employee.model.ts
        │   ├── project.model.ts
        │   └── matching.model.ts
        └── services/               # HTTP service wrappers
            ├── auth.service.ts
            ├── employee.service.ts
            ├── project.service.ts
            ├── matching.service.ts
            └── toast.service.ts
```

---

## Features

### Authentication
- **Signup** — Register as a `PROJECT_MANAGER` or `EMPLOYEE`
- **Login** — Receive a JWT token valid for 24 hours
- **Forgot / Reset Password** — Token-based password reset flow

### Employee Management
- Create, view, update, and delete employee profiles
- Each employee has **Skills** (with proficiency levels) and **Certifications**
- Availability statuses: `AVAILABLE` · `PARTIALLY_AVAILABLE` · `UNAVAILABLE`
- Experience levels: `JUNIOR` · `MID` · `SENIOR` · `LEAD`
- Composite **Employee Score** stored on the record for quick ranking

### Project Management *(Project Manager only)*
- Create, update, and delete projects with domain, location preferences, and date ranges
- Define per-project **Requirements**: each requirement maps to a `Role` with required skills, certifications, experience level, years of experience, expected salary, and work mode

### Smart Matching *(Project Manager only)*
- `GET /matching/{projectId}?k=10` returns the top-K best-matching available employees
- Employees are scored against every requirement of the project; the highest requirement-score is used
- Results are sorted in descending order of matching score

### Assignment Management *(Project Manager only)*
- Assign an available employee to a project — employee status is updated automatically
- Unassign an employee — restores their availability status
- Manager dashboard: full overview of all projects and their current assignments

### Employee Dashboard
- Employees can view their currently assigned projects
- Ranking dashboard displays scored/matched employees (read-only)

---

## Matching Algorithm

The matching score is a value between **0 and 100** calculated using five weighted criteria:

| Criterion | Weight | Scoring Logic |
|---|---|---|
| **Skill Match** | 40 pts | Proportion of the role's required skills that the employee possesses |
| **Certification Match** | 20 pts | Proportion of the role's required certifications that the employee holds |
| **Experience Level** | 20 pts | Full points if employee level ≥ required; 5-pt penalty per level below |
| **Years of Experience** | 10 pts | Full points if years ≥ required; proportional otherwise |
| **Salary Fit** | 10 pts | Employee expected salary vs project budget for the role |

**Formula:**
```
score = skillScore(0–40)
      + certScore(0–20)
      + experienceLevelScore(0–20)
      + yearsScore(0–10)
      + salaryScore(0–10)
```

Only employees with `AVAILABLE` or `PARTIALLY_AVAILABLE` status are considered. The final ranked list is truncated to the top-K results (default: 10).

---

## API Reference

All endpoints (except `/auth/**`) require a `Authorization: Bearer <token>` header.

### Auth — `/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/signup` | Public | Register a new user |
| `POST` | `/auth/login` | Public | Authenticate and receive JWT |
| `POST` | `/auth/forgot-password` | Public | Generate a password-reset token |
| `POST` | `/auth/reset-password` | Public | Reset password using token |

**Signup request body:**
```json
{
  "username": "john.doe",
  "password": "Secret@123",
  "role": "PROJECT_MANAGER"
}
```

**Login response body:**
```json
{
  "token": "<JWT>",
  "username": "john.doe",
  "role": "PROJECT_MANAGER"
}
```

---

### Employees — `/employees`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/employees` | Any | List all employees |
| `GET` | `/employees/{id}` | Any | Get employee by database ID |
| `GET` | `/employees/eid/{employeeId}` | Any | Get employee by employee ID string |
| `GET` | `/employees/available` | Any | List employees with AVAILABLE status |
| `POST` | `/employees` | Any | Create a new employee |
| `PUT` | `/employees/{id}` | Any | Update an employee |
| `DELETE` | `/employees/{id}` | Any | Delete an employee |

---

### Projects — `/projects`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/projects` | Any | List projects (managers see own; others see all) |
| `GET` | `/projects/{id}` | Any | Get project by ID |
| `POST` | `/projects` | Manager | Create a project |
| `PUT` | `/projects/{id}` | Manager | Update a project |
| `DELETE` | `/projects/{id}` | Manager | Delete a project |
| `GET` | `/projects/{id}/requirements` | Any | List requirements for a project |
| `POST` | `/projects/{id}/requirements` | Manager | Add a requirement to a project |
| `PUT` | `/projects/requirements/{reqId}` | Manager | Update a requirement |
| `DELETE` | `/projects/requirements/{reqId}` | Manager | Delete a requirement |

---

### Matching — `/matching`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/matching/{projectId}?k=10` | Manager | Top-K ranked employees for a project |

---

### Assignments — `/assignments`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/assignments` | Manager | Assign an employee to a project |
| `DELETE` | `/assignments/{id}` | Manager | Remove an assignment |
| `GET` | `/assignments/project/{projectId}` | Manager | All assignments for a project |
| `GET` | `/assignments/dashboard` | Manager | Full manager dashboard data |
| `GET` | `/assignments/my-projects` | Authenticated | Employee's own assigned projects |

---

## Data Models

### Employee
```
id, employeeId (unique), name, joiningDate,
experienceLevel (JUNIOR|MID|SENIOR|LEAD),
yearsOfExperience, preferredLocation,
availabilityStatus (AVAILABLE|PARTIALLY_AVAILABLE|UNAVAILABLE),
previousRatings, expectedSalary, employeeScore,
skills[], certifications[]
```

### Skill
```
id, name, proficiencyLevel (BEGINNER|INTERMEDIATE|ADVANCED|EXPERT)
```

### Certification
```
id, certificateId, name, issuingOrganization, score
```

### Project
```
id, projectName, domain, locationPreferences[],
startDate, endDate, projectRequirements[]
```

### ProjectRequirement
```
id, location, numberOfPositions, role
```

### Role
```
id, name, experienceLevel, yearsOfExperience,
expectedSalary, workMode (REMOTE|HYBRID|ONSITE),
requiredSkills[], certificationsNeeded[]
```

### ProjectAssignment
```
id, project, employee, assignedAt
```

---

## Frontend Pages & Routes

| Route | Component | Guard | Description |
|---|---|---|---|
| `/` | `HomeComponent` | None | Landing / welcome page |
| `/login` | `LoginComponent` | None | Login form |
| `/signup` | `SignupComponent` | None | Registration form |
| `/forgot-password` | `ForgotPasswordComponent` | None | Request password reset |
| `/reset-password` | `ResetPasswordComponent` | None | Submit new password |
| `/employees` | `EmployeeFormComponent` | `authGuard` | Create / edit employee profile |
| `/my-dashboard` | `EmployeeDashboardComponent` | `authGuard` | View assigned projects (employee) |
| `/ranking` | `RankingDashboardComponent` | `authGuard` | Employee ranking view |
| `/projects` | `ProjectRequirementComponent` | `managerGuard` | Manage projects & requirements |
| `/matching` | `MatchingResultsComponent` | `managerGuard` | View matching results per project |
| `/manager-dashboard` | `ManagerDashboardComponent` | `managerGuard` | Full assignment overview |

**Route Guards:**
- `authGuard` — allows any authenticated (logged-in) user
- `managerGuard` — allows only users with the `PROJECT_MANAGER` role; redirects employees to `/ranking`

---

## Authentication & Authorization

### JWT Flow
1. User logs in via `POST /auth/login`
2. Server returns a signed JWT (HS256, 24-hour expiry)
3. Angular's `AuthInterceptor` attaches the token to every outgoing request as `Authorization: Bearer <token>`
4. `JwtAuthFilter` validates the token on each request and populates the Spring Security context

### Roles
| Role | Access |
|---|---|
| `EMPLOYEE` | Auth-protected pages: employee form, own dashboard, ranking |
| `PROJECT_MANAGER` | All employee pages **plus** project management, matching, manager dashboard |

---

## Getting Started

### Prerequisites
| Tool | Minimum Version |
|---|---|
| Java JDK | 21 |
| Apache Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |
| Angular CLI | 19 (`npm install -g @angular/cli`) |
| MySQL Server | 8.0+ |

---

### 1. Database Setup

Create a MySQL user (or use an existing one) and ensure the server is running. The application will **automatically create** the `sras_db` database on first run thanks to `createDatabaseIfNotExist=true` in the datasource URL.

```sql
-- Optional: create a dedicated user
CREATE USER 'sras_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON sras_db.* TO 'sras_user'@'localhost';
FLUSH PRIVILEGES;
```

---

### 2. Backend Setup

```bash
cd sras-backend

# Build (skips tests for a quick start)
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

The API will be available at **http://localhost:8081**.

On first run, Hibernate (`ddl-auto=update`) creates all required tables automatically.

---

### 3. Frontend Setup

```bash
cd sras-ui

# Install dependencies
npm install

# Start development server
ng serve
```

The application will be available at **http://localhost:4200**.

The `proxy.conf.json` proxies all `/api/**` requests to `http://localhost:8081`, eliminating CORS issues during development.

---

### 4. First-Time Usage

1. Open **http://localhost:4200**
2. Click **Sign Up** and create a `PROJECT_MANAGER` account
3. Create a second `EMPLOYEE` account for testing
4. As the manager, navigate to **Projects** and create a project with requirements
5. Navigate to **Matching** to view the ranked employee list for any project
6. Assign an employee and view the result in the **Manager Dashboard**
7. Log in as the employee and check **My Dashboard** to see the assigned project

---

## Environment Configuration

### Backend — `src/main/resources/application.properties`

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/sras_db` | MySQL connection URL |
| `spring.datasource.username` | `root` | Database username |
| `spring.datasource.password` | `root` | Database password |
| `jwt.secret` | *(hardcoded)* | HS256 signing key — **change in production** |
| `jwt.expiration` | `86400000` | Token TTL in milliseconds (24 hours) |
| `server.port` | `8081` | API server port |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema generation strategy |

> **Security Note:** Replace the `jwt.secret` with a securely generated 256-bit key before deploying to any non-development environment. Never commit real credentials to source control.

### Frontend — `src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081'
};
```

For production builds, update `environment.prod.ts` with the deployed API URL and run:
```bash
ng build --configuration production
```

---

## Running Tests

### Backend Unit Tests

```bash
cd sras-backend
./mvnw test
```

Test reports are written to `target/surefire-reports/`. The test suite covers all five controllers:
- `AuthControllerTest`
- `EmployeeControllerTest`
- `ProjectControllerTest`
- `MatchingControllerTest`
- `AssignmentControllerTest`

### Frontend Unit Tests

```bash
cd sras-ui
ng test
```

Runs Karma + Jasmine tests in a Chrome browser.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## License

This project is proprietary software developed as part of the TalentSync initiative at Cognizant. All rights reserved.
