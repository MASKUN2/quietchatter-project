# Member 및 Customer 서비스 통합 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `microservice-customer` 서비스를 `microservice-member` 서비스로 통합하여 단일 서비스로 운영합니다.

**Architecture:** 헥사고날 아키텍처를 유지하며 `com.quietchatter.customer` 패키지를 `microservice-member` 내부에 병합합니다. `CustomerMessage`의 ID 체계를 `UUID`로 통일합니다.

**Tech Stack:** Kotlin, Spring Boot, Spring Data JPA, Flyway, Spring Cloud Gateway

---

### Task 1: 의존성 및 설정 업데이트

**Files:**
- Modify: `microservice-member/build.gradle.kts`
- Modify: `microservice-api-gateway/src/main/resources/application.yml`

- [ ] **Step 1: microservice-member에 validation 의존성 추가**
`microservice-customer`에서 사용하던 유효성 검사를 위해 추가합니다.

```kotlin
// microservice-member/build.gradle.kts
dependencies {
    // ... 기존 의존성
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // ...
}
```

- [ ] **Step 2: API Gateway 라우팅 설정 변경**
`/v1/customer/**` 요청을 `member-service`로 전달하도록 수정합니다.

```yaml
# microservice-api-gateway/src/main/resources/application.yml
            - id: customer-service
              uri: ${MEMBER_SERVICE_URL:http://localhost:8083} # 8082에서 8083으로 변경
              predicates:
                - Path=/v1/customer/**
```

- [ ] **Step 3: 변경 사항 커밋**
```bash
git add microservice-member/build.gradle.kts microservice-api-gateway/src/main/resources/application.yml
git commit -m "chore: update dependencies and gateway routing for customer integration"
```

---

### Task 2: 도메인 및 영속성 코드 이동

**Files:**
- Create: `microservice-member/src/main/kotlin/com/quietchatter/customer/domain/CustomerMessage.kt`
- Create: `microservice-member/src/main/kotlin/com/quietchatter/customer/application/out/CustomerMessageRepository.kt`
- Create: `microservice-member/src/main/kotlin/com/quietchatter/customer/adaptor/out/persistence/CustomerMessageJpaRepository.kt`
- Create: `microservice-member/src/main/resources/db/migration/V3__create_customer_message_table.sql`

- [ ] **Step 1: CustomerMessage 도메인 생성 (UUID 기반)**
`member` 서비스의 `BaseEntity`를 상속받도록 수정합니다.

```kotlin
package com.quietchatter.customer.domain

import com.quietchatter.member.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "customer_message")
class CustomerMessage(
    @Column(name = "message", columnDefinition = "TEXT")
    var message: String
) : BaseEntity()
```

- [ ] **Step 2: 레포지토리 인터페이스 및 구현체 생성**

```kotlin
// application/out/CustomerMessageRepository.kt
package com.quietchatter.customer.application.out
import com.quietchatter.customer.domain.CustomerMessage
interface CustomerMessageRepository {
    fun save(customerMessage: CustomerMessage): CustomerMessage
}

// adaptor/out/persistence/CustomerMessageJpaRepository.kt
package com.quietchatter.customer.adaptor.out.persistence
import com.quietchatter.customer.application.out.CustomerMessageRepository
import com.quietchatter.customer.domain.CustomerMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerMessageJpaRepository : JpaRepository<CustomerMessage, UUID>, CustomerMessageRepository
```

- [ ] **Step 3: Flyway 마이그레이션 파일 생성**
ID 타입을 UUID로 변경하여 테이블을 생성합니다.

```sql
-- microservice-member/src/main/resources/db/migration/V3__create_customer_message_table.sql
CREATE TABLE IF NOT EXISTS customer_message (
    id UUID PRIMARY KEY,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP NOT NULL
);
```

- [ ] **Step 4: 변경 사항 커밋**
```bash
git add microservice-member/src/main/kotlin/com/quietchatter/customer/ microservice-member/src/main/resources/db/migration/V3__create_customer_message_table.sql
git commit -m "feat: migrate customer domain and persistence to member service"
```

---

### Task 3: 애플리케이션 및 웹 레이어 이동

**Files:**
- Create: `microservice-member/src/main/kotlin/com/quietchatter/customer/application/in/CustomerMessageCreatable.kt`
- Create: `microservice-member/src/main/kotlin/com/quietchatter/customer/application/service/CustomerMessageService.kt`
- Create: `microservice-member/src/main/kotlin/com/quietchatter/customer/adaptor/in/web/CustomerMessageController.kt`

- [ ] **Step 1: 유스케이스 및 서비스 구현**

```kotlin
// application/in/CustomerMessageCreatable.kt
package com.quietchatter.customer.application.in
interface CustomerMessageCreatable {
    fun create(command: CreateCommand)
    data class CreateCommand(val content: String)
}

// application/service/CustomerMessageService.kt
package com.quietchatter.customer.application.service
import com.quietchatter.customer.application.in.CustomerMessageCreatable
import com.quietchatter.customer.application.out.CustomerMessageRepository
import com.quietchatter.customer.domain.CustomerMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CustomerMessageService(
    private val customerMessageRepository: CustomerMessageRepository
) : CustomerMessageCreatable {
    override fun create(command: CustomerMessageCreatable.CreateCommand) {
        customerMessageRepository.save(CustomerMessage(command.content))
    }
}
```

- [ ] **Step 2: 컨트롤러 구현**

```kotlin
package com.quietchatter.customer.adaptor.in.web

import com.quietchatter.customer.application.in.CustomerMessageCreatable
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/customer")
class CustomerMessageController(
    private val customerMessageCreatable: CustomerMessageCreatable
) {
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@RequestBody @Valid request: CreateRequest) {
        customerMessageCreatable.create(CustomerMessageCreatable.CreateCommand(request.content))
    }

    data class CreateRequest(
        @field:Size(min = 1, max = 500)
        val content: String
    )
}
```

- [ ] **Step 3: 변경 사항 커밋**
```bash
git add microservice-member/src/main/kotlin/com/quietchatter/customer/
git commit -m "feat: migrate customer application and web layers to member service"
```

---

### Task 4: 기존 서비스 삭제 및 검증

- [ ] **Step 1: microservice-customer 디렉토리 삭제**
```bash
rm -rf microservice-customer
```

- [ ] **Step 2: microservice-member 빌드 및 테스트 실행**
```bash
cd microservice-member
./gradlew clean build
```

- [ ] **Step 3: 변경 사항 커밋**
```bash
git add .
git commit -m "cleanup: remove integrated microservice-customer"
```
