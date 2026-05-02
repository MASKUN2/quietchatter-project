# 내부 API 기반 Talk 작성자 정보 동기화 실행 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `microservice-talk`에서 Talk 작성 시 `microservice-member`의 내부 API를 호출하여 작성자 닉네임을 가져와 저장하는 구조를 구현합니다.

**Architecture:** 
- **Member**: `/internal/**` 전용 컨트롤러 및 DTO 추가
- **Talk**: Spring Cloud OpenFeign을 사용하여 Member 서비스 호출 및 스냅샷 저장
- **Gateway**: `/internal/**` 경로에 대한 외부 접근 차단 필터 적용

**Tech Stack:** Kotlin (Spring Boot), Spring Cloud OpenFeign, Git

---

### Task 1: Member 서비스 Internal API 구현

**Files:**
- Create: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in/web/internal/MemberInternalController.kt`
- Create: `microservice-member/src/main/kotlin/com/quietchatter/member/dto/InternalMemberResponse.kt`

- [ ] **Step 1: Internal DTO 생성**

```kotlin
package com.quietchatter.member.dto
import java.util.UUID

data class InternalMemberResponse(
    val id: UUID,
    val nickname: String
)
```

- [ ] **Step 2: Internal Controller 생성**

```kotlin
package com.quietchatter.member.adaptor.`in`.web.internal

import com.quietchatter.member.application.MemberService
import com.quietchatter.member.dto.InternalMemberResponse
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/internal/api/members")
class MemberInternalController(
    private val memberService: MemberService
) {
    @GetMapping("/{memberId}")
    fun getMemberInfo(@PathVariable memberId: UUID): InternalMemberResponse {
        val member = memberService.findById(memberId) ?: throw NoSuchElementException("Member not found")
        return InternalMemberResponse(member.id!!, member.nickname)
    }
}
```

- [ ] **Step 3: 빌드 및 커밋**

```bash
cd microservice-member && ./gradlew build && git add . && git commit -m "feat(member): add internal API for member info"
```

---

### Task 2: Talk 서비스 Feign Client 연동 및 로직 수정

**Files:**
- Modify: `microservice-talk/build.gradle.kts`
- Create: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/out/external/MemberClient.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/TalkService.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/TalkController.kt`

- [ ] **Step 1: OpenFeign 의존성 및 설정 추가**

`build.gradle.kts`에 `spring-cloud-starter-openfeign` 추가 및 `TalkApplication.kt`에 `@EnableFeignClients` 추가.

- [ ] **Step 2: Feign Client 인터페이스 생성**

```kotlin
package com.quietchatter.talk.adaptor.out.external

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import java.util.UUID

@FeignClient(name = "member-service", url = "\${MEMBER_SERVICE_URL:http://microservice-member:8083}")
interface MemberClient {
    @GetMapping("/internal/api/members/{memberId}")
    fun getMemberInfo(
        @PathVariable("memberId") memberId: UUID,
        @RequestHeader("X-Internal-Secret") secret: String = "default-internal-secret"
    ): InternalMemberResponse
}

data class InternalMemberResponse(val id: UUID, val nickname: String)
```

- [ ] **Step 3: TalkService 수정 (정보 조회 로직 추가)**

`createTalk` 시 `memberClient.getMemberInfo` 호출 후 결과 사용.

- [ ] **Step 4: TalkController 수정 (X-Member-Nickname 헤더 제거)**

`createTalk` 메서드에서 `@RequestHeader("X-Member-Nickname")` 파라미터 제거.

- [ ] **Step 5: 빌드 및 커밋**

```bash
cd microservice-talk && ./gradlew build && git add . && git commit -m "feat(talk): integrate Feign client and sync author snapshot"
```

---

### Task 3: API Gateway 내부 경로 차단 설정

**Files:**
- Modify: `microservice-api-gateway/src/main/kotlin/com/quietchatter/gateway/AuthenticationFilter.kt`

- [ ] **Step 1: /internal/** 경로 차단 로직 추가**

`doFilterInternal` 시작 부분에 요청 URI가 `/internal/`로 시작하면 `403 Forbidden` 반환 로직 추가.

- [ ] **Step 2: 빌드 및 커밋**

```bash
cd microservice-api-gateway && ./gradlew build && git add . && git commit -m "security(gateway): block external access to /internal endpoints"
```

---

### Task 4: 최종 검증 및 아카이브

- [ ] **Step 1: 전체 프로젝트 빌드 확인**

- [ ] **Step 2: 최종 커밋**

```bash
git add . && git commit -m "feat: complete internal API sync for talk author info"
```
