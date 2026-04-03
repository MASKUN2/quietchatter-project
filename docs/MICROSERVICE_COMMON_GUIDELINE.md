# 마이크로서비스 공통 개발 지침 (Microservice Common Development Guideline)

이 문서는 기획된 모든 마이크로서비스 서브모듈에 공통으로 적용되는 개발 표준 및 운영 원칙을 정의합니다. 모든 AI 에이전트와 개발자는 이를 준수해야 합니다.

## 1. 기술 스택 (Technical Stack)

* 언어: Kotlin 1.9.x
* 프레임워크: Spring Boot 3.5.13
* 서비스 발견 및 설정: Consul (Discovery & Config)
* 데이터베이스: PostgreSQL (JPA), Redis (Cache/Session)
* 빌드 도구: Gradle (Kotlin DSL)

## 2. 아키텍처 패턴 (Architecture Pattern)

모든 서비스(Gateway 제외)는 헥사고날 아키텍처(Hexagonal Architecture, Ports and Adapters)를 따릅니다.

* domain: 외부 의존성 없는 순수 비즈니스 로직 (Entity, Value Object)
* application: 유스케이스 정의 (Port 인터페이스 및 Implementation)
* adaptor/in: 외부 요청 진입점 (RestController, Request/Response DTO)
* adaptor/out: 외부 시스템 연동 (JPA Repository, Redis Client, API Client)

## 3. 코딩 표준 (Coding Standards)

* Idiomatic Kotlin: Kotlin의 특성을 살린 코드를 작성합니다.
* No Lombok: Lombok을 사용하지 않고 Kotlin의 data class나 기본 기능을 활용합니다.
* Null Safety: Kotlin의 Null 안전성을 적극 활용하며, `!!` 연산자 사용을 지양합니다.
* 비동기 처리: 필요한 경우 Coroutines를 사용하되, 라이브러리 지원(WebFlux 등)과 조화롭게 작성합니다.

## 4. 테스트 및 검증 (Testing & Verification)

* 단위 테스트 필수: 코드를 작성하거나 수정할 때마다 반드시 관련 로직에 대한 단위 테스트(Unit Test)를 함께 작성해야 합니다.
* 통과 확인: 모든 테스트가 통과하는지 확인한 후 변경사항을 반영합니다.

## 5. 인프라 설정 (Infrastructure)

### 서비스별 포트 할당
* 8080: microservice-gateway
* 8081: microservice-user
* 8082: microservice-book
* 8083: microservice-talk
* 8084: microservice-customer

### 메모리 최적화
* 저사양 인프라(t4g.nano/micro) 환경을 고려하여 JVM 메모리 설정을 최적화합니다. 세부 설정은 docs/spring-boot-memory-optimization-guide.md를 따릅니다.

## 6. 보안 및 인증 (Security & Authentication)

* 게이트웨이 인증: 모든 외부 요청은 Gateway에서 JWT 검증을 거칩니다.
* 내부 통신: 각 서비스는 Gateway가 주입한 `X-User-Id` 헤더를 신뢰하여 사용자 식별에 사용합니다.
* 민감 정보: API Key 등은 Consul Config 또는 환경 변수를 통해 관리하며 코드에 노출하지 않습니다.

## 7. 문서 및 답변 규칙 (Documentation Rules)

* 강조 서식 금지: 마크다운 작성 시 굵게(bold)나 기울임(italics) 서식을 절대 사용하지 않습니다.
* 이모티콘 금지: 문서나 답변 내에 이모티콘을 사용하지 않습니다.
* 언어: 한국어 또는 고등학생 수준의 쉬운 영어를 사용합니다.
