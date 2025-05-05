# Task Tracker

A RESTful task management API built with Spring Boot, featuring role‑based access control (RBAC), JWT authentication, and interactive API documentation via Swagger/OpenAPI.

---

## How to Run the Application

### Prerequisites

- Java 17+
- Maven 3.8+ (or use the Maven Wrapper)
- PostgreSQL

### Setup

1. **Clone the repository**

   ```bash
   git clone <repo-url>
   cd task-tracker
   ```

2. **Set required environment variables**

   | Variable | Example | Purpose |
      |----------|---------|---------|
   | `POSTGRES_USERNAME` | `postgres` | Database user |
   | `POSTGRES_PASSWORD` | `secret`   | Database password |
   | `JWT_SECRET`        | `nVHytbtOalZWrBQMhPUMLVJKvdbngnV62WOw0qFLuYs=` | HMAC key used to sign JWTs |

   ```bash
   # macOS / Linux
   export POSTGRES_USERNAME=postgres
   export POSTGRES_PASSWORD=secret
   export JWT_SECRET=nVHytbtOalZWrBQMhPUMLVJKvdbngnV62WOw0qFLuYs=
   ```

   *(IntelliJ IDEA → Run/Debug Configuration → Environment variables)*

3. **Build & run**

   ```bash
   ./mvnw clean package
   java -jar target/task-tracker-0.0.1-SNAPSHOT.jar
   # or
   ./mvnw spring-boot:run
   ```

4. **Swagger / OpenAPI**

   - UI → <http://localhost:8080/swagger-ui.html>
   - JSON → <http://localhost:8080/v3/api-docs>

---

## Roles and Permissions Overview

| Role | Key Abilities |
|------|---------------|
| **ADMIN** | • Full CRUD on projects & tasks <br> • Create users with any role <br> • View all tasks (global) <br> • Assign tasks across any project <br> • Change any project owner |
| **MANAGER** | • CRUD only on projects they own <br> • Create & delete tasks within their projects <br> • Assign/unassign tasks in their projects <br> • View tasks in their projects |
| **USER** | • View tasks assigned to themselves <br> • Update **only** the status of their tasks |

---

## API Endpoint Summary

### Auth & Users

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/auth/register` | Public | Register new **USER** |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| POST | `/api/admin/users` | ADMIN | Create user (ADMIN / MANAGER / USER) |

### Projects

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/projects` | MANAGER, ADMIN | Create project |
| GET | `/api/projects/{id}` | Owner, ADMIN | Get project |
| GET | `/api/projects` | MANAGER, ADMIN | List projects (MANAGER sees own) |
| PUT | `/api/projects/{id}` | MANAGER, ADMIN | Update project (owner change by ADMIN) |
| DELETE | `/api/projects/{id}` | MANAGER, ADMIN | Delete project |

### Tasks

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/tasks` | MANAGER, ADMIN | Create task |
| GET | `/api/tasks/project/{projectId}` | Owner, ADMIN | Paginated + filtered tasks by project |
| GET | `/api/tasks` | Assignee, Owner, ADMIN | Paginated + filtered tasks by assignee |
| GET | `/api/tasks/{id}` | Assignee, Owner, ADMIN | Get task |
| PUT | `/api/tasks/{id}` | Assignee, Owner, ADMIN | Update task |
| PUT | `/api/tasks/{id}/assign` | MANAGER (own project), ADMIN | Assign task |
| PUT | `/api/tasks/{id}/status` | Assignee | Update status only |
| DELETE | `/api/tasks/{id}` | Assignee, Owner, ADMIN | Delete task |

_List endpoints accept `status`, `priority`, `page`, `size` query params._

---

## Authentication Flow

1. **Register** → `/api/auth/register` (creates USER)
2. **Login** → `/api/auth/login`

   ```json
   { "email": "user@example.com", "password": "secret" }
   ```

   Response returns **the JWT**:

   ```json
   "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   ```

3. **Use the token**

   ```http
   Authorization: Bearer <JWT>
   ```

4. `JwtAuthenticationFilter` validates signature & expiry with `JWT_SECRET`, loads user details, and sets the security context.
