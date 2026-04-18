# QuietChatter Project

AI 에이전트를 위한 지침은 AGENTS.md 파일을 참고하십시오.

quietchatter-project는 마이크로서비스 아키텍처(MSA) 기반의 효율적이고 안전한 채팅 서비스 인프라 및 애플리케이션 프로젝트입니다.

## 인프라 아키텍처 (Infrastructure)

본 프로젝트는 AWS 환경에서 비용 최적화와 데이터 보존을 최우선으로 설계되었습니다.

### 주요 구성 요소
- Network: VPC 내 퍼블릭/프라이빗 서브넷 분리 (ap-northeast-2)
- NAT & Ingress Node: EC2 기반 NAT 구성 및 NGINX Ingress Docker 운영 (비용 절감형)
- API Gateway: Spring Cloud Gateway 기반 microservice-gateway 운영 (JWT 검증 및 라우팅)
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
├── microservice-gateway/    # Spring Cloud Gateway (라우팅 및 보안)
├── microservice-member/     # 회원 및 인증 마이크로서비스
├── microservice-book/       # 도서 정보 마이크로서비스
├── microservice-talk/       # 북톡 및 반응 마이크로서비스
├── microservice-customer/   # 고객 지원 마이크로서비스
```

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
