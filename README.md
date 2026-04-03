# QuietChatter Project

quietchatter-project는 마이크로서비스 아키텍처(MSA) 기반의 효율적이고 안전한 채팅 서비스 인프라 및 애플리케이션 프로젝트입니다.

## 인프라 아키텍처 (Infrastructure)

본 프로젝트는 AWS 환경에서 비용 최적화와 데이터 보존을 최우선으로 설계되었습니다.

### 주요 구성 요소
- Network: VPC 내 퍼블릭/프라이빗 서브넷 분리 (ap-northeast-2)
- NAT & Ingress Node: EC2 기반 NAT 구성 및 NGINX Ingress Docker 운영 (비용 절감형)
- API Gateway Node: Docker 기반 API Gateway 운영 (향후 Spring Cloud Gateway 도입 예정)
- Persistence Node: 
  - Docker Compose 기반 관리: PostgreSQL 16, Redis 7, Redpanda (Kafka 호환)
  - EBS 데이터 분리: 15GB 독립 EBS 볼륨을 /data에 마운트하여 인스턴스 재생성 시에도 데이터 안전 보장
- Microservices: 향후 ASG(Auto Scaling Group)와 스팟 인스턴스를 활용한 비용 절감형 노드 구성 예정

## 기술 스택

- IaC: Terraform (HCL)
- Container: Docker, Docker Compose
- OS: Amazon Linux 2023 (ARM64, t4g series)
- Database: PostgreSQL, Redis
- Messaging: Redpanda
- Framework: Spring Boot (예정), Spring Cloud (예정)

## 프로젝트 구조

```text
.
├── infrastructure/          # 테라폼 기반 인프라 정의 (IaC)
│   ├── docker-compose.*.yaml # 노드별 서비스 구성 파일
│   └── *.tf                  # AWS 리소스 정의
├── microservice-book/       # 도서 관련 마이크로서비스 (서브모듈)
└── microservice-user/       # 사용자 관련 마이크로서비스 (서브모듈)
```

## 개발 지침 및 문서 (Documentation)

AI 에이전트 및 개발자를 위한 주요 설계 가이드와 개발 규약 문서입니다. 작업을 시작하기 전 다음 문서들을 참고하십시오.

- [전체 프로젝트 개발 규약 (CONVENTIONS.md)](docs/CONVENTIONS.md)
- [아키텍처 비전 및 지향점 (ARCHITECTURE_VISION.md)](docs/ARCHITECTURE_VISION.md)
- [백엔드 설계 원칙 (BACKEND_DESIGN.md)](docs/BACKEND_DESIGN.md)
- [스프링 부트 메모리 최적화 가이드 (spring-boot-memory-optimization-guide.md)](docs/spring-boot-memory-optimization-guide.md)
- [최근 작업 및 변경 이력 요약 (LAST_WORK_SUMMARY.md)](docs/LAST_WORK_SUMMARY.md)

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
