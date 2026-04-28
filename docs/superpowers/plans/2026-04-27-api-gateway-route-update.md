# API Gateway - Route & Filter Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize API paths to use the `/api/{resource}` pattern in the API Gateway.

**Architecture:** Update Spring Cloud Gateway route configurations in `application.yml` and synchronize the authentication logic in `AuthenticationFilter.kt` to handle the new path patterns.

**Tech Stack:** Kotlin, Spring Boot, Spring Cloud Gateway

---

### Task 1: Update Gateway Routes

**Files:**
- Modify: `microservice-api-gateway/src/main/resources/application.yml`

- [ ] **Step 1: Update routes in application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: microservice-api-gateway
  # ... other settings ...
  cloud:
    gateway:
      server:
        webmvc:
          routes:
            - id: auth-service
              uri: ${MEMBER_SERVICE_URL:http://localhost:8083}
              predicates:
                - Path=/api/auth/**
            - id: member-service
              uri: ${MEMBER_SERVICE_URL:http://localhost:8083}
              predicates:
                - Path=/api/members/**
            - id: support-service
              uri: ${MEMBER_SERVICE_URL:http://localhost:8083}
              predicates:
                - Path=/api/support/**
            - id: book-service
              uri: ${BOOK_SERVICE_URL:http://localhost:8081}
              predicates:
                - Path=/api/books/**
            - id: talk-service
              uri: ${TALK_SERVICE_URL:http://localhost:8084}
              predicates:
                - Path=/api/talks/**,/api/reactions/**
```

---

### Task 2: Update Authentication Filter

**Files:**
- Modify: `microservice-api-gateway/src/main/kotlin/com/quietchatter/gateway/AuthenticationFilter.kt`

- [ ] **Step 1: Update bypassPaths and optionalPaths in AuthenticationFilter.kt**

```kotlin
    private val bypassPaths = listOf("/api/auth/login", "/api/auth/signup", "/api/auth/reactivate", "/api/support", "/actuator/health")
    private val optionalPaths = listOf("/api/books", "/api/talks", "/api/members/me")
```

---

### Task 3: Verification

**Files:**
- N/A

- [ ] **Step 1: Run build to verify compilation**

Run: `./gradlew :microservice-api-gateway:build -x test`
Expected: BUILD SUCCESSFUL

---

### Task 4: Commit Changes

- [ ] **Step 1: Commit the changes**

Run: `git add microservice-api-gateway && git commit -m "feat(gateway): update routes and auth filter for /api/{resource} standardization"`
