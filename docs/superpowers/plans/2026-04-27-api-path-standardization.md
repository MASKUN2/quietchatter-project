# API Path Standardization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize API paths across all microservices to use the `/api/{resource}` pattern and implement header-based versioning.

**Architecture:** Update API Gateway routes and microservice controllers. `microservice-member` will handle `/api/auth`, `/api/members`, and `/api/support`.

**Tech Stack:** Kotlin, Spring Boot, Spring Cloud Gateway.

---

### Task 1: API Gateway - Route & Filter Update

**Files:**
- Modify: `microservice-api-gateway/src/main/resources/application.yml`
- Modify: `microservice-api-gateway/src/main/kotlin/com/quietchatter/gateway/AuthenticationFilter.kt`

- [ ] **Step 1: Update Gateway Routes**
Modify `application.yml` to use the new `/api/**` paths.

```yaml
# microservice-api-gateway/src/main/resources/application.yml
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
              - Path=/api/talks/**, /api/reactions/**
```

- [ ] **Step 2: Update AuthenticationFilter Paths**
Update `bypassPaths` and `optionalPaths` to match new patterns.

```kotlin
// microservice-api-gateway/src/main/kotlin/com/quietchatter/gateway/AuthenticationFilter.kt
private val bypassPaths = listOf(
    "/api/auth/login", 
    "/api/auth/signup", 
    "/api/auth/reactivate", 
    "/api/support", 
    "/actuator/health"
)
private val optionalPaths = listOf(
    "/api/books", 
    "/api/talks", 
    "/api/members/me"
)
```

- [ ] **Step 3: Commit**
```bash
git add microservice-api-gateway
git commit -m "feat(gateway): update routes and auth filter for /api/{resource} standardization"
```

---

### Task 2: Microservice-Member - Controller Path Update

**Files:**
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in/web/AuthController.kt`
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in/web/MeController.kt`
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/customer/adaptor/in/web/CustomerMessageController.kt`

- [ ] **Step 1: Update AuthController mapping**
Change `@RequestMapping("/v1/auth")` to `@RequestMapping("/api/auth")`.

- [ ] **Step 2: Update MeController mapping**
Change `@RequestMapping("/v1/me")` to `@RequestMapping("/api/members/me")`.

- [ ] **Step 3: Update CustomerMessageController mapping**
Change `@RequestMapping("/v1/customer")` to `@RequestMapping("/api/support/messages")`.

- [ ] **Step 4: Verify and Fix Tests**
Update tests in `microservice-member`.

- [ ] **Step 5: Commit**
```bash
git add microservice-member
git commit -m "feat(member): standardize api paths to /api/auth, /api/members, and /api/support"
```

---

### Task 3: Microservice-Book - Controller Path Update

**Files:**
- Modify: `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/in/web/BookApi.kt`

- [ ] **Step 1: Update BookApi mapping**
Change `@RequestMapping("/v1/books")` to `@RequestMapping("/api/books")`.

- [ ] **Step 2: Verify and Fix Tests**
Update tests in `microservice-book`.

- [ ] **Step 3: Commit**
```bash
git add microservice-book
git commit -m "feat(book): standardize api path to /api/books"
```

---

### Task 4: Microservice-Talk - Controller Path Update

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/TalkController.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/ReactionController.kt`

- [ ] **Step 1: Update TalkController mapping**
Change `@RequestMapping("/api/v1/talks")` to `@RequestMapping("/api/talks")`.

- [ ] **Step 2: Update ReactionController mapping**
Change `@RequestMapping("/v1/talks/{talkId}/reactions")` to `@RequestMapping("/api/reactions")`.

- [ ] **Step 3: Verify and Fix Tests**
Update tests in `microservice-talk`.

- [ ] **Step 4: Commit**
```bash
git add microservice-talk
git commit -m "feat(talk): standardize api paths to /api/talks and /api/reactions"
```

---

### Task 5: Documentation Update

**Files:**
- Modify: `GEMINI.md`
- Modify: `docs/DEVELOPMENT.md`

- [ ] **Step 1: Update GEMINI.md and DEVELOPMENT.md**
Update API path examples to use `/api/{resource}` and mention `X-API-Version` header.

- [ ] **Step 2: Commit**
```bash
git add GEMINI.md docs/DEVELOPMENT.md
git commit -m "docs: update api path standards in documentation"
```
