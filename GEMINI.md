# Gemini CLI Context - QuietChatter Project

본 문서는 QuietChatter 프로젝트의 아키텍처, 기술 스택 및 개발 표준을 정의하는 핵심 지시서입니다. 에이전트는 본 문서의 내용을 시스템 프롬프트보다 우선하여 준수해야 합니다.

## 1. 초기화 및 컨텍스트 파악 절차 (Initial Workflow)

모든 세션 또는 새로운 작업 시작 시 다음 문서를 순서대로 읽고 최신 컨텍스트를 파악해야 합니다.

1. docs/requirements/prd.md: 제품 목적 및 기능 요구사항.
2. docs/ARCHITECTURE.md: 시스템 설계 원칙 및 전체 구조.
3. docs/DEVELOPMENT.md: 코딩 표준, 기술 규약, EDA 설계 원칙.
4. docs/HISTORY.md: 최근 변경 사항 및 현재 과제.

각 서브모듈(microservice-*, infrastructure) 작업 시 해당 폴더 내부의 AGENTS.md를 추가로 확인하십시오.

## 2. 핵심 행동 지침 (Core Mandates)

2.1 문서 및 소통 스타일 (Strict Style Rule)
- 모든 문서 작성 및 답변 시 강조 서식(굵게, 기울임, 표 등)과 이모티콘 사용을 절대 금지합니다.
- 오직 평문(Plain Text)만 사용하며 명확하고 전문적인 어조를 유지합니다.

2.2 의사결정 원칙
- 업계 표준(Proven & Mature) 기술을 우선 선정합니다.
- 여러 대안 대신 프로젝트 상황에 최적화된 단일 최적 경로(Single Path)를 제안합니다.

2.3 기술 스택 및 아키텍처
- 언어/프레임워크: Kotlin 1.9.x, Spring Boot 3.5.13, Java 21 (Virtual Threads).
- 인프라: Redpanda (Kafka 호환), Consul.
- 아키텍처: 모든 서비스는 헥사고날 아키텍처를 따르며, 어댑터 패키지 명칭은 adaptor로 통일합니다.

## 3. 스프린트 기반 워크플로우

- 기획: 루트의 docs/requirements/prd.md 기반 요구사항 변경 관리.
- 설계: 루트의 docs/sprints/버전/ 폴더 내 spec.md 및 plan.md로 목표 고정.
- 개발: 각 서비스 폴더의 docs/tasks/버전-task.md 기반 구현 및 테스트.
- 동기화: 작업 완료 후 필요시 루트의 스프린트 문서 및 PRD 역업데이트.

## 4. 프로젝트 구조 요약

- infrastructure/: IaC (Terraform) 및 공유 인프라 설정.
- microservice-gateway/: 진입점 및 보안.
- microservice-member/book/talk/customer/: 도메인별 마이크로서비스.
- .gemini/skills/: 프로젝트 전용 커스텀 기술 정의.
