# 프로젝트 개발 지침 (Development Guideline)

본 문서는 마이크로서비스 개발을 위한 코딩 표준, 아키텍처 패턴, 기술 설정 및 최적화 규칙을 정의합니다. 모든 개발자는 이를 준수해야 합니다.

## 1. 아키텍처 패턴 (Hexagonal Architecture)

모든 서비스(Gateway 제외)는 헥사고날 아키텍처(Ports and Adapters)를 따릅니다.

- domain: 외부 의존성 없는 순수 비즈니스 로직 (Entity, Value Object).
- application: 유스케이스 정의 (Port 인터페이스 및 Service 구현체).
- adaptor/in: 외부 요청 진입점 (RestController, DTO).
- adaptor/out: 외부 시스템 연동 (JPA Repository, API Client, Redis).

## 2. 코딩 및 문서 규칙

### 2.1 Kotlin 표준
- Idiomatic Kotlin: 언어의 특성을 살린 간결한 코드를 작성합니다.
- No Lombok: Kotlin의 data class를 사용하며 Lombok 도입을 금지합니다.
- Null Safety: 안전한 연산자를 사용하고 `!!` 사용을 지양합니다.

### 2.2 패키지 및 파일 명명
- 패키지명은 소문자와 케밥 케이스(kebab-case)를 조합하여 사용합니다.
- 외부 연동 어댑터 패키지 명칭은 반드시 adaptor를 사용합니다.

### 2.3 문서 스타일
- 마크다운 작성 시 강조 서식(굵게, 기울임 등)을 절대 사용하지 않습니다.
- 문서 및 답변 내에서 이모티콘 사용을 엄격히 금지합니다.
- 모든 텍스트는 평문(Plain Text)으로 작성하며, 명확하고 간결한 문장을 사용하여 가독성을 높입니다.

## 3. 서비스 간 통신 (Communication)

### 3.1 직접 호출 (Synchronous)
- 실시간 응답이 필요한 경우 Spring RestClient를 사용합니다.
- Consul LoadBalancer를 연계하여 서비스 이름을 통해 호출합니다.

### 3.2 비동기 이벤트 통신 (Asynchronous / EDA)
서비스 간 결합도를 낮추고 데이터 전파가 목적인 경우 비동기 이벤트를 우선 고려합니다. 메시징 브로커로 Redpanda를 사용하며 Spring Cloud Stream을 통해 구현합니다.

- 발행: 트랜잭셔널 아웃박스 패턴을 적용하여 DB 작업과 이벤트 발행의 원자성을 보장합니다.
- 토픽 설계: `{도메인명}`(예: `member`) 형식을 사용하며, 상세 이벤트명은 메시지 본문에 포함합니다.
- 순서 보장: 엔티티 ID를 메시지 키(Message Key)로 사용하여 동일 엔티티에 대한 이벤트의 순차 처리를 보장합니다.
- 에러 핸들링: 지수 백오프 기반의 계층적 재시도를 수행하며, 최종 실패 시 `{토픽명}.dlq`로 격리하여 수동 분석합니다.

## 4. 데이터 저장소 및 마이그레이션

- 데이터베이스 분리: 서비스별로 독립된 논리 데이터베이스를 사용하며, application.yml에서 명확히 구분합니다 (예: `jdbc:postgresql://.../microservice-talk`).
- Flyway: 각 서비스는 `src/main/resources/db/migration`에 독립적인 마이그레이션 스크립트를 관리합니다.
- JPA Auditing: 메인 클래스와 분리하여 `config/JpaConfig`에서 별도로 설정합니다.

## 5. 인프라 및 최적화

### 5.1 포트 및 실행 환경
- 모든 마이크로서비스는 컨테이너 내부 기본 포트 8080을 사용합니다.
- 각 서비스는 독립된 노드 또는 컨테이너 환경에서 실행됨을 전제로 합니다.

### 5.2 메모리 및 성능 최적화
- 가상 스레드: `spring.threads.virtual.enabled: true` 설정을 통해 I/O 효율을 높입니다.
- JVM 튜닝: 저사양 인프라를 고려하여 SerialGC를 사용하고 힙 메모리를 엄격히 제한합니다.
- 연결 제한: 톰캣의 `max-connections`와 `accept-count`를 보수적으로 설정하여 급격한 메모리 폭증을 방지합니다.

## 6. API 문서화 (RestDocs & OpenAPI)

- Test-Driven Documentation: 모든 API는 Spring RestDocs를 사용하여 테스트 통과 시에만 문서가 생성되도록 합니다.
- OpenAPI 3.0: restdocs-api-spec을 통해 YAML 형식의 스펙을 자동 추출합니다.
- 런타임 제공: 각 서비스는 `/api/v1/spec` 엔드포인트를 통해 최신 YAML 스펙을 반환해야 합니다.

## 7. AI 에이전트 작업 및 의사결정 원칙 (AI Agent Principles)

AI 에이전트는 기술적 검토나 제안 시 다음 원칙을 엄격히 준수합니다.

- 업계 표준 우선 (Industry Standard First): 기술 선정 시 실험적인 기술보다 업계에서 널리 사용되고 커뮤니티 지원이 활발한 기술(Proven & Mature)을 최우선으로 선택합니다.
- 단일 경로 제안 (Single Path Proposal): 여러 대안을 나열하여 사용자에게 선택을 넘기기보다, 현재 프로젝트 상황에 가장 적합한 단 하나의 최적안(Best Practice)을 도출하여 제안합니다.
- 직관적 결론: 기술적 근거는 간결하게 제시하되, 곧바로 실행 가능한 결론을 중심으로 보고합니다.

