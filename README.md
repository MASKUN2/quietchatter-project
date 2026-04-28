# QuietChatter Project

AI 에이전트를 위한 지침은 AGENTS.md 파일을 참고하십시오.

quietchatter-project는 마이크로서비스 아키텍처(MSA) 기반의 효율적이고 안전한 채팅 서비스 인프라 및 애플리케이션 프로젝트입니다.

## 인프라 아키텍처 (Infrastructure)

본 프로젝트는 AWS 환경에서 비용 최적화와 데이터 보존을 최우선으로 설계되었습니다.

### 주요 구성 요소
- Network: VPC 내 퍼블릭/프라이빗 서브넷 분리 (ap-northeast-2)
- NAT & Ingress Node: EC2 기반 NAT 구성 및 NGINX Ingress Docker 운영 (비용 절감형)
- API Gateway: Spring Cloud Gateway 기반 microservice-api-gateway 운영 (JWT 검증 및 라우팅)
- Persistence Node: 
  - Docker Compose 기반 관리: PostgreSQL 16, Redis 7, Redpanda (Kafka 호환), Consul Server
  - EBS 데이터 분리: 15GB 독립 EBS 볼륨을 /data에 마운트하여 인스턴스 재생성 시에도 데이터 안전 보장
- Microservices: 
  - 서비스 검색: 각 노드에 배치된 Consul Client Agent를 통해 서비스 탐색 및 헬스 체크 수행
  - 포트 설정: 모든 서비스는 컨테이너 내부 포트 8080을 기본으로 사용하며, Consul을 통해 동적으로 탐색됨
  - 인스턴스: 향후 ASG(Auto Scaling Group)와 스팟 인스턴스를 활용한 비용 절감형 노드 구성 예정

## 기술 스택

- IaC: Terraform (HCL)
- Container: Docker, Docker Compose
- Discovery: HashiCorp Consul (Agent-based Architecture)
- OS: Amazon Linux 2023 (ARM64, t4g series)
- Database: PostgreSQL, Redis
- Messaging: Redpanda
- Backend: Spring Boot 3.5.13, Spring Cloud 2025.x
- Frontend & BFF: Next.js 15, MUI v6, TypeScript, jose (JWT)

## 프로젝트 구조

```text
.
├── infrastructure/          # 테라폼 기반 인프라 정의 (IaC)
├── microservice-frontend/   # Next.js 15 기반 웹 프론트엔드 및 BFF
├── microservice-api-gateway/    # Spring Cloud Gateway (라우팅 및 보안)
├── microservice-member/     # 회원, 인증 및 고객 지원(Support) 마이크로서비스
├── microservice-book/       # 도서 정보 마이크로서비스
├── microservice-talk/       # 북톡 및 반응 마이크로서비스
├── legacy-quiet-chatter/    # (Archive) 레거시 자바 백엔드 (참조용)
├── legacy-quiet-chatter-front-end/ # (Archive) 레거시 리액트 프론트 (참조용)
└── legacy-quiet-chatter-docs/ # (Archive) 레거시 기획/정책 문서 (참조용)
```

**주의:** `legacy-`로 시작하는 모든 폴더는 보관(Archive) 상태입니다. 새로운 마이크로서비스 구현을 위한 로직 참조용으로만 사용하며, 직접적인 코드 수정이나 커밋은 엄격히 금지됩니다.

## API 명세 및 경로 표준화 (API Path Standardization)

본 프로젝트는 서비스 간 결합도를 낮추고 일관된 접근을 위해 리소스 중심의 API 경로 표준을 따릅니다. 모든 요청은 `/api` 접두사로 시작하며, 버전 관리는 URL이 아닌 헤더를 통해 수행합니다.

### 1. 표준 API 경로 매핑

| 리소스 (Resource) | 담당 서비스 | 경로 패턴 (Path Pattern) |
| :--- | :--- | :--- |
| **인증 (Auth)** | `microservice-member` | `/api/auth/**` |
| **회원 (Members)** | `microservice-member` | `/api/members/**` |
| **고객지원 (Support)** | `microservice-member` | `/api/support/**` |
| **도서 (Books)** | `microservice-book` | `/api/books/**` |
| **북톡 (Talks)** | `microservice-talk` | `/api/talks/**` |
| **반응 (Reactions)** | `microservice-talk` | `/api/reactions/**` |

### 2. 버전 관리 전략
- **헤더 기반 버전 관리**: `X-API-Version` 헤더를 사용합니다. (기본값: `1`)
- **버전 표기**: 정수 형태 (예: `1`, `2`)를 사용하며, URL 경로에는 `v1`, `v2` 등을 포함하지 않습니다.

### 3. API 스펙 관리 전략 (Test-Driven Documentation)
본 프로젝트는 코드와 문서의 불일치를 방지하기 위해 **테스트 주도 문서화** 방식을 채택합니다.

-   **Spring RestDocs + OpenAPI 3.0**: 모든 API는 컨트롤러 테스트(JUnit 5)를 통해 실제 동작이 검증된 경우에만 문서화됩니다. `testDocs` 태스크 실행 시 `epages restdocs-api-spec`을 통해 `openapi3.yaml` 파일이 자동 생성됩니다.
-   **실시간 스펙 제공**: 각 서비스는 `/api/{resource}/spec` 경로를 통해 최신 OpenAPI 스펙을 제공하며, 이는 API Gateway를 통해 통합 조회 및 문서 도구(Swagger UI 등) 연동에 사용됩니다.
-   **단일 진입점 (Aggregator)**: 프론트엔드 개발자의 편의를 위해 API Gateway에서 모든 마이크로서비스의 스펙을 하나로 병합하여 제공합니다.
    -   **통합 스펙 주소**: `GET /api/docs/openapi.yaml` (YAML 형식)
    -   **특징**: 서비스 간 모델명 충돌 방지를 위해 각 스키마에 서비스 접두어(예: `Member_`, `Book_`)가 자동으로 부여됩니다.

## CI/CD 및 배포 전략 (S3 Bridge Pattern)

본 프로젝트는 소규모 MSA 환경에 최적화된 **S3 Bridge 패턴(GitOps-lite)**을 통해 배포를 자동화합니다. 각 서비스는 독립된 저장소에서 관리되지만, S3를 매개체로 하여 중앙 집중식 배포 제어를 달성합니다.

### 1. 배포 흐름 (Deployment Flow)
1.  **이미지 빌드**: 개발자가 코드를 `main` 브랜치에 푸시하면 GitHub Actions가 트리거되어 최신 Docker 이미지를 빌드합니다.
2.  **SHA 태그 생성**: 빌드된 이미지에는 Git 커밋 해시(SHA) 기반의 고유 태그가 부여됩니다 (예: `sha-a1b2c3d`).
3.  **매니페스트 자동 업데이트**: GitHub Actions가 서비스 내부의 `k8s/deployment.yaml` 파일을 읽어 이미지 태그를 방금 생성한 SHA로 자동 치환합니다.
4.  **S3 브릿지 전송**: 수정된 매니페스트를 중앙 S3 버킷(`quietchatter-infra-assets`)의 지정된 경로로 업로드합니다.
5.  **자동 동기화**: 쿠버네티스 Controlplane 노드에서 5분마다 실행되는 `sync.sh` 스크립트가 S3의 변경 사항을 감지하고 `kubectl apply`를 통해 클러스터에 반영합니다.

### 2. 주요 장점
-   **독립적 자치권**: 각 마이크로서비스는 인프라 저장소에 대한 접근 권한 없이도 자신의 S3 경로를 통해 안전하게 배포를 진행할 수 있습니다.
-   **추적성**: 모든 배포 파일이 Git 커밋 SHA를 태그로 사용하므로, 운영 환경에 떠 있는 코드가 어떤 버전인지 즉시 확인이 가능합니다.
-   **안정성**: 중앙에서 `sync.sh`를 통해 모든 매니페스트를 통합 관리하므로 설정의 일관성을 유지할 수 있습니다.

## 개발 지침 및 문서 (Documentation)

AI 에이전트 및 개발자를 위한 핵심 가이드 문서입니다. 작업을 시작하기 전 반드시 숙지하십시오.

- [아키텍처 가이드 (ARCHITECTURE.md)](docs/ARCHITECTURE.md): 프로젝트 비전, 설계 원칙, 서비스 구성.
- [개발 지침 가이드 (DEVELOPMENT.md)](docs/DEVELOPMENT.md): 코딩 표준, 기술 설정, EDA, 최적화 규칙.
- [프로젝트 이력 (HISTORY.md)](docs/HISTORY.md): 최근 작업 요약 및 변경 이력.

## 시작하기 (Infrastructure)

### 사전 준비
- Terraform 설치
- AWS CLI 설정 및 자격 증명(Credentials) 완료

### 배포 순서
1. cd infrastructure
2. terraform init
3. terraform plan  # 변경 사항 확인
4. terraform apply # 실제 인프라 배포

## 주요 결정 사항 및 노트
- 비용 최적화: AWS Managed 서비스(NAT Gateway, ALB) 대신 EC2 + Docker 조합을 선택하여 고정 비용 최소화.
- 데이터 보호: DB 노드의 EBS 볼륨을 분리하여 terraform apply로 인한 인스턴스 교체 시에도 데이터 유지.
- 확장 전략: 마이크로서비스 노드는 스팟 인스턴스를 활용하여 온디맨드 대비 최대 90% 비용 절감 추구.
