# Spec: API Path Standardization and Header-based Versioning

This specification defines the new API path structure and versioning strategy to ensure consistency, scalability, and loose coupling between microservices.

## 1. Core Principles

- **Resource-Centric:** URLs must represent resources using plural nouns.
- **Unified Prefix:** All API endpoints must start with `/api` to distinguish them from frontend routes.
- **Path Unification:** Internal implementation details (like microservice names) are hidden from the URL.
- **Header Versioning:** The `X-API-Version` header is used for versioning, keeping URLs clean and persistent.

## 2. API Path Structure

The standard format is: `/api/{resource}/**`

### 2.1 Mapping Table

| Resource | Service | New Path Pattern | Old Path(s) |
| :--- | :--- | :--- | :--- |
| **Auth** | `microservice-member` | `/api/auth/**` | `/v1/auth/**` |
| **Members** | `microservice-member` | `/api/members/**` | `/v1/me/**` |
| **Support (VOC)** | `microservice-member` | `/api/support/**` | `/v1/customer/**` |
| **Books** | `microservice-book` | `/api/books/**` | `/v1/books/**` |
| **Talks** | `microservice-talk` | `/api/talks/**` | `/v1/talks/**` |
| **Reactions** | `microservice-talk` | `/api/reactions/**` | `/v1/reactions/**` |

## 3. Versioning Strategy

### 3.1 Header Specification
- **Header Name:** `X-API-Version`
- **Format:** Integer (e.g., `1`, `2`)
- **Default Value:** `1`

### 3.2 Responsibility
- **Gateway:** Responsible for routing based on the `{resource}` segment.
    - `/api/auth` -> `member-service`
    - `/api/members` -> `member-service`
    - `/api/support` -> `member-service`
- **Microservices:** Responsible for reading the header and executing the appropriate logic version.

## 4. Specific Path Recommendations (Member Service)

The `microservice-member` handles three distinct top-level API resources:

| Resource | Functionality | Example |
| :--- | :--- | :--- |
| **`/api/auth`** | Login, Signup, Token Refresh | `/api/auth/login`, `/api/auth/signup` |
| **`/api/members`** | User Profile, Account Management | `/api/members/me`, `/api/members/{id}` |
| **`/api/support`** | VOC, Customer Messages | `/api/support/messages` |

## 5. Implementation Strategy

### 5.1 Gateway Configuration (Spring Cloud Gateway)
The gateway will use `Path` predicates.

**Gateway Routing Example:**
- Route `auth`: `/api/auth/**` -> `lb://microservice-member`
- Route `members`: `/api/members/**` -> `lb://microservice-member`
- Route `support`: `/api/support/**` -> `lb://microservice-member`

### 5.2 Microservice Controller Example (Kotlin)
```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> { ... }
}

@RestController
@RequestMapping("/api/members")
class MemberController {
    @GetMapping("/me")
    fun getMe(): ResponseEntity<Any> { ... }
}
```

## 6. Testing & Validation
- **Gateway Routing:** Verify that `/api/auth/login` and `/api/support/messages` correctly reach the `member` service.
- **Header Propagation:** Verify that the `X-API-Version` header is present.
