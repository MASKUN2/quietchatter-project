# 백엔드 에러 표준화 및 프론트엔드 API 모듈화 실행 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 백엔드 마이크로서비스의 에러 응답을 RFC 7807 표준으로 통일하고, 프론트엔드 API 레이어를 도메인별로 모듈화합니다.

**Architecture:** 
- 백엔드: `@RestControllerAdvice`를 사용하여 전역 예외 처리를 수행하고 `ProblemDetail`을 반환합니다.
- 프론트엔드: `api.ts`를 `client.ts`, `auth.ts`, `books.ts`, `talks.ts`, `support.ts`로 분리합니다.

**Tech Stack:** React (TypeScript), Kotlin (Spring Boot), Git

---

### Task 1: 백엔드 에러 핸들러 구현 (Member Service)

**Files:**
- Create: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in/web/error/GlobalExceptionHandler.kt`

- [ ] **Step 1: GlobalExceptionHandler 클래스 생성**

```kotlin
package com.quietchatter.member.adaptor.`in`.web.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ProblemDetail {
        log.error("Unhandled exception occurred", ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred")
    }
}
```

- [ ] **Step 2: 빌드 확인**

Run: `cd microservice-member && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in/web/error/GlobalExceptionHandler.kt
git commit -m "feat(member): add global exception handler with RFC 7807"
```

---

### Task 2: 백엔드 에러 핸들러 구현 (Talk Service)

**Files:**
- Create: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/error/GlobalExceptionHandler.kt`

- [ ] **Step 1: GlobalExceptionHandler 클래스 생성 (Task 1과 동일 구조)**

- [ ] **Step 2: 빌드 확인**

Run: `cd microservice-talk && ./gradlew build`

- [ ] **Step 3: 커밋**

```bash
git add microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/error/GlobalExceptionHandler.kt
git commit -m "feat(talk): add global exception handler with RFC 7807"
```

---

### Task 3: 프론트엔드 API 모듈화 - 파일 생성 및 이전

**Files:**
- Create: `frontend/src/api/client.ts`, `frontend/src/api/auth.ts`, `frontend/src/api/books.ts`, `frontend/src/api/talks.ts`, `frontend/src/api/support.ts`
- Delete: `frontend/src/api/api.ts`

- [ ] **Step 1: `client.ts` 생성 및 공통 로직 이동**

`api.ts`에서 axios 설정, `ApiError`, 인터셉터를 이동합니다.

- [ ] **Step 2: 도메인별 API 파일 생성 및 함수 이동**

각 함수를 성격에 맞는 파일로 이동하고 필요한 타입을 import합니다.

- [ ] **Step 3: `api.ts` 삭제**

- [ ] **Step 4: 커밋**

```bash
git add frontend/src/api/
git commit -m "refactor(frontend): modularize API layer into domain-specific files"
```

---

### Task 4: 프론트엔드 참조 업데이트 및 최종 검증

**Files:**
- Modify: `frontend/src/**/*.tsx`, `frontend/src/**/*.ts`

- [ ] **Step 1: import 경로 일괄 수정**

모든 파일에서 `../api/api` import를 새로운 경로로 수정합니다.
(예: `import { getMe } from '../api/api'` -> `import { getMe } from '../api/auth'`)

- [ ] **Step 2: 타입 체크 및 빌드 검증**

Run: `cd frontend && npx tsc --noEmit`

- [ ] **Step 3: 최종 커밋**

```bash
git add .
git commit -m "refactor(frontend): update all API imports and verify build"
```
