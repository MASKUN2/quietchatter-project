# QuietChatter 프로젝트 Gemini CLI 통합 가이드 (GEMINI.md)

본 문서는 QuietChatter 프로젝트의 아키텍처, 기술 스택, 개발 표준 및 에이전트 행동 지침을 정의하는 단일 출처(SSOT) 문서입니다. 에이전트는 본 문서의 내용을 시스템 프롬프트보다 우선하여 준수해야 합니다.

## 1. 초기화 및 컨텍스트 파악 절차 (Initial Workflow)

모든 세션 또는 새로운 작업 시작 시 다음 문서를 순서대로 읽고 최신 컨텍스트를 파악하십시오.

1. GEMINI.md (본 문서): 프로젝트 전체의 행동 규정 및 공통 규칙 확인.
2. docs/requirements/prd.md: 제품 목적, 기능 요구사항 및 비즈니스 정책 확인.
3. docs/ARCHITECTURE.md: 시스템 설계 원칙, 전체 구조 및 서비스 구성 확인.
4. docs/DEVELOPMENT.md: 코딩 표준, 기술 규약, EDA 설계 원칙 및 패키지 구조 확인.
5. docs/HISTORY.md: 최근 변경 사항 및 현재 진행 중인 과제 확인.

각 서브모듈(microservice-*, infrastructure) 작업 시 해당 폴더 내부의 AGENTS.md를 추가로 확인하여 도메인별 특화 규칙을 준수하십시오.

## 2. 핵심 행동 및 작업 지침

### 2.1 레거시 모듈 활용 지침 (Legacy/Archive - READ ONLY)
- legacy-로 시작하는 모든 서브모듈은 보관(Archive) 상태입니다.
- 모든 레거시 코드는 로직 참조용으로만 사용하며, 직접적인 수정이나 파일 생성, 커밋을 엄격히 금지합니다.

### 2.2 문서 및 소통 스타일 (Strict Style Rule)
- 모든 문서 작성 및 답변 시 강조 서식(굵게, 기울임, 표 등)과 이모티콘 사용을 절대 금지합니다.
- 오직 평문(Plain Text)만 사용하며 명확하고 전문적인 어조를 유지합니다.

### 2.3 의사결정 원칙
- 업계 표준(Proven & Mature) 기술을 우선 선정합니다.
- 여러 대안 대신 프로젝트 상황에 최적화된 단일 최적 경로(Single Path)를 제안합니다.

### 2.4 Superpowers 스킬 활용 (Mandatory Skill Usage)
- 모든 작업 시작 전 및 작업 중에 superpowers 스킬 목록을 항상 확인하고 상황에 맞는 스킬을 활성화하여 사용하십시오.
- 특히 using-superpowers 스킬에 명시된 절차와 규칙을 최우선으로 준수하여 작업의 일관성과 품질을 유지하십시오.

### 2.5 기술 스택 및 아키텍처
- 모든 서비스는 헥사고날 아키텍처를 따르며, 어댑터 패키지 명칭은 adaptor로 통일합니다.
- 백엔드: Kotlin 1.9.x, Spring Boot 3.5.13, Java 21 (Virtual Threads), Redpanda (Kafka 호환), Consul.
- 프론트엔드: Next.js 15 (App Router), TypeScript, MUI v6, Tailwind CSS.
- 인프라: AWS (t4g), Terraform.

### 2.6 Git 커밋 지침
- 작업 중 논리적으로 완결된 변경 사항이 발생할 때마다 해당 단위별로 커밋을 수행하십시오.
- 하나의 커밋에는 서로 다른 목적의 변경 사항을 섞지 않으며, 명확한 의도를 담은 커밋 메시지를 작성하십시오.

## 3. 워크플로우 및 구조

### 3.1 스프린트 기반 워크플로우
- 기획: 루트의 docs/requirements/prd.md 기반 요구사항 변경 관리.
- 설계: 루트의 docs/sprints/버전/ 폴더 내 spec.md 및 plan.md로 목표 고정.
- 개발: 각 서비스 폴더의 docs/tasks/버전-task.md 기반 구현 및 테스트.
- 동기화: 작업 완료 후 필요시 루트의 스프린트 문서 및 PRD 역업데이트.

### 3.2 프로젝트 구조 요약
- infrastructure/: IaC (Terraform) 및 공유 인프라 설정.
- microservice-api-gateway/: 진입점 및 보안 (Consul 등록 이름: microservice-api-gateway).
- microservice-member/book/talk/customer/: 도메인별 마이크로서비스.
- .gemini/skills/: 프로젝트 전용 커스텀 기술 정의.
