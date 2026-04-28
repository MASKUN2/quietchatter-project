# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

- 각 서브모듈에 있는 CLAUDE.md가 있으면 참조하십시오. 

## 필독 문서 (작업 전 순서대로 확인)

1. docs/requirements/prd.md - 제품 목적 및 기능 요구사항
2. docs/ARCHITECTURE.md - 시스템 설계 원칙 및 서비스 구조
3. docs/DEVELOPMENT.md - 코딩 표준, EDA 설계, AI 의사결정 원칙
4. docs/HISTORY.md - 최근 변경 이력 및 진행 중인 과제
5. 작업 대상 서비스의 AGENTS.md - 도메인별 세부 지침
6. 작업 대상 서비스의 docs/tasks/ 폴더 - 스프린트 태스크

## 제품 정체성

QuietChatter는 내향적인 독자들을 위한 저자극 도서 공유 SNS 플랫폼입니다.

핵심 가치:
- 익명성: 사용자 신원을 드러내지 않는 구조
- 휘발성: 콘텐츠 자동 숨김 처리
- 저자극: 비언어적 공감 중심 (좋아요 등 반응 위주)

## 프로젝트 구조

모놀리식 레거시를 MSA로 전환하는 프로젝트입니다. 각 마이크로서비스는 독립된 Git 서브모듈로 관리됩니다.

- microservice-api-gateway: 단일 진입점. Spring Cloud Gateway로 JWT 검증 및 라우팅 처리
- microservice-member: 회원가입, 인증/인가(OAuth), JWT 발급
- microservice-book: 외부 도서 API 연동 및 캐싱
- microservice-talk: 북톡(대화), 반응 처리 및 비즈니스 자동화
- microservice-customer: 고객 센터 문의 및 CS 처리
- infrastructure: Terraform 기반 AWS 인프라 (계층 구조 01~03)
- legacy-*: 참조용 아카이브 (수정 금지)

## 레거시 서브모듈 사용 규칙

legacy-quiet-chatter, legacy-quiet-chatter-front-end, legacy-quiet-chatter-batch, legacy-quiet-chatter-docs는 모두 archive 상태입니다.

- 읽기 전용으로만 사용합니다. 어떤 경우에도 파일을 수정하거나 커밋하지 마십시오.
- 마이크로서비스 포팅 시 비즈니스 로직, 패키지 구조, 테스트 패턴의 참조 소스로 활용합니다.
- 레거시 코드를 그대로 복사하지 말고, idiomatic Kotlin으로 재작성하십시오.

## 빌드 및 테스트 명령

각 마이크로서비스 폴더에서 실행합니다.

```
# 빌드
./gradlew build

# 전체 테스트
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.quietchatter.{service}.{패키지}.{클래스명}"

# 로컬 실행
./gradlew bootRun

# API 문서 생성용 테스트만 실행 (restdocs 태그)
./gradlew testDocs

# OpenAPI 스펙 생성 (testDocs 후 자동 실행)
./gradlew openapi3

# bootJar 빌드 (openapi3 포함)
./gradlew bootJar
```

## 헥사고날 아키텍처 패키지 구조

Gateway를 제외한 모든 서비스는 이 구조를 따릅니다.

```
com.quietchatter.{service}/
  domain/          - 외부 의존성 없는 순수 비즈니스 로직 (Entity, Value Object)
  application/
    in/            - 유스케이스 Port 인터페이스 (입력)
    out/           - 외부 연동 Port 인터페이스 (출력)
  adaptor/
    in/web/        - RestController, DTO (HTTP 진입점)
    in/messaging/  - Kafka Consumer (이벤트 수신)
    in/scheduler/  - 스케줄러
    out/persistence/ - JPA Repository 구현체
    out/outbox/    - 트랜잭셔널 아웃박스 구현체
    out/external/  - 외부 API 클라이언트
  config/          - JpaConfig, SecurityConfig 등 설정 분리
```

어댑터 패키지명은 반드시 `adaptor`(adapter 아님)를 사용합니다.

## 핵심 설계 규칙

인증 흐름: Gateway에서 JWT를 중앙 검증하고, `X-Member-Id` 헤더에 회원 ID를 삽입하여 내부 서비스로 전파합니다. 내부 서비스는 이 헤더를 신뢰합니다.

DB 격리: 서비스 간 DB 교차 참조 절대 금지. 각 서비스는 독립 논리 DB를 사용합니다.

EDA (비동기 이벤트):
- 서비스 간 데이터 전파는 Redpanda(Kafka 호환) + Spring Cloud Stream 사용
- 이벤트 발행은 반드시 트랜잭셔널 아웃박스 패턴 적용
- 토픽명 형식: `{도메인명}` (예: `member`), 세부 이벤트명은 메시지 본문에 포함
- 메시지 키는 엔티티 ID 사용 (순서 보장)
- 최종 실패 시 `{토픽명}.dlq`로 격리

서비스 탐색: Consul 사용. 동기 호출 시 Spring RestClient + Consul LoadBalancer로 서비스 이름 기반 호출.

## Kotlin 규칙

- data class 사용, Lombok 금지
- `!!` 사용 지양, 안전 연산자(`?.`, `?:`) 사용
- 패키지명은 소문자 + 케밥케이스 조합

## API 문서화

- Spring RestDocs + restdocs-api-spec으로 테스트 통과 시에만 문서 생성
- 문서 생성 테스트에는 `@Tag("restdocs")` 적용
- 각 서비스는 `/api/v1/spec` 엔드포인트로 최신 OpenAPI YAML 스펙 제공

## 인프라 작업 순서 (Terraform)

```
cd infrastructure
terraform init
terraform plan
terraform apply
```

계층 실행 순서: 01-base → 02-platform → 03-apps

## 문서 작성 규칙

강조 서식(굵게, 기울임) 및 이모티콘 사용 절대 금지. 평문(Plain Text)만 사용합니다.

## AI 의사결정 원칙

- 업계 표준 기술 우선 선택 (실험적 기술 지양)
- 여러 대안 나열 대신 단일 최적안을 제안
- 기술 근거는 간결하게, 실행 가능한 결론 중심으로 보고
