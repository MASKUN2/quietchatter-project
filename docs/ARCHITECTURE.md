# 프로젝트 아키텍처 가이드 (Project Architecture Guide)

본 문서는 QuietChatter 프로젝트의 궁극적인 목표, 시스템 설계 원칙, 그리고 마이크로서비스 아키텍처(MSA) 전환 전략을 정의합니다.

## 1. 프로젝트 개요 및 목적

기존의 모놀리식 시스템을 현대적인 마이크로서비스 아키텍처로 전면 개편하여 시스템의 확장성, 유지보수성, 그리고 배포의 안정성을 확보하는 것을 목표로 합니다.

## 2. 시스템 구성 및 역할

도메인별 응집도와 배포의 독립성을 고려하여 다음과 같이 서비스를 구성하며, 각 서비스는 독립된 Git 서브모듈 저장소로 관리합니다.

### 2.1 마이크로서비스 (Microservices)
- microservice-gateway: 모든 요청의 단일 진입점. Spring Cloud Gateway 기반의 라우팅 및 통합 보안(JWT 검증) 처리.
- microservice-member: 회원 가입, 인증/인가(OAuth), 프로필 및 권한 관리.
- microservice-book: 외부 도서 API 연동 및 책 정보 캐싱.
- microservice-talk: 회원 간 대화, 반응(좋아요 등) 처리 및 비즈니스 자동화.
- microservice-customer: 고객 센터 문의 접수 및 CS 업무 처리.

### 2.2 인프라 및 레거시 (Infrastructure & Legacy)
- infrastructure: Terraform 기반의 AWS 인프라 정의 및 Docker Compose 설정 관리.
- legacy-modules: 기존 백엔드, 프론트엔드, 배치 시스템 및 문서 아카이브 참조용.

## 3. 핵심 설계 원칙

### 3.1 기술 스택 표준
- 언어 및 프레임워크: Kotlin 1.9.x + Spring Boot 3.5.13 + Spring Cloud 2025.x.
- 동시성 모델: Java 21 가상 스레드(Virtual Threads)를 기본으로 사용하여 I/O 효율성을 극대화합니다.
- 서비스 탐색: HashiCorp Consul을 통한 동적 서비스 등록 및 설정 관리.

### 3.2 인증 및 보안 전략
- 게이트웨이에서 JWT 유효성을 중앙 검증합니다.
- 검증된 사용자 정보를 `X-Member-Id` 등의 헤더에 삽입하여 내부 서비스로 전파합니다.
- 내부 서비스는 게이트웨이가 주입한 헤더 정보를 신뢰하여 회원 식별에 사용합니다.

### 3.3 데이터 및 통신 원칙
- 데이터베이스 격리: 각 서비스는 독립된 논리 데이터베이스를 사용하며, 교차 참조는 엄격히 금지됩니다.
- 비동기 우선 (EDA): 서비스 간 강한 결합을 피하기 위해 Redpanda(Kafka 호환)를 활용한 비동기 이벤트를 적극 도입합니다.
- 일관성 보장: 트랜잭셔널 아웃박스 패턴을 사용하여 DB 작업과 이벤트 발행의 원자성을 보장합니다.

## 4. MSA 전환 전략 및 현황

- [x] 기반 서비스 포팅: 레거시 핵심 로직의 마이크로서비스 이관 완료.
- [x] 인프라 자동화: IaC(Terraform) 기반의 재현 가능한 환경 구축 완료.
- [x] API 계약 자동화: Spring RestDocs 기반의 OpenAPI 3.0 스펙 제공 표준화 완료.
- [ ] 가시성 확보: Prometheus 및 Grafana 연동을 통한 모니터링 체계 구축 예정.
- [ ] 회복력 강화: Resilience4j 기반의 서킷 브레이커 도입 예정.
