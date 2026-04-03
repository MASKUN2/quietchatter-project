# 프로젝트 목표 및 아키텍처 비전 (Project Goals and Architecture Vision)

이 문서는 quietchatter-project의 궁극적인 목표와 이전(Legacy) 시스템에서 마이크로서비스 아키텍처(MSA)로의 전환 계획을 AI 에이전트와 개발자가 이해하기 쉽도록 정리한 것입니다.

## 1. 프로젝트 개요 및 목적

기존의 모놀리식 및 분산된 레거시 시스템을 통합하고, 현대적인 마이크로서비스 아키텍처(MSA)로 전면 개편하는 것이 본 프로젝트의 핵심 목표입니다. 대대적인 개선을 통해 시스템의 확장성, 유지보수성, 그리고 배포의 안정성을 확보하고자 합니다.

## 2. 프로젝트 저장소 구조

본 프로젝트는 메인 저장소(quietchatter-project)에서 Git 서브모듈 방식을 통해 다음 저장소들을 통합 관리합니다.

### 2.1 마이크로서비스 (Microservices)
- microservice-user: 회원 및 인증 도메인
- microservice-book: 도서 정보 및 검색 도메인
- microservice-talk: 대화 및 반응 도메인
- microservice-customer: 고객 지원 및 상담 도메인
- microservice-gateway: API 라우팅 및 통합 보안 게이트웨이

### 2.2 인프라 및 레거시 (Infrastructure & Legacy)
- infrastructure: 테라폼 기반 AWS 인프라 정의
- legacy-quiet-chatter: 기존 모놀리식 백엔드 참조용
- legacy-quiet-chatter-front-end: 기존 프론트엔드 참조용
- legacy-quiet-chatter-batch: 기존 배치 시스템 참조용
- legacy-quiet-chatter-docs: 기존 프로젝트 문서 아카이브

## 3. 주요 개선 방향 및 전략

- IaC(Infrastructure as Code) 기반 구축: 인프라 구성을 코드로 관리하여 재현성과 안정성을 확보합니다. 인프라 구축부터 시작하여 전체 시스템의 기반을 단단히 다집니다.
- 마이크로서비스 아키텍처(MSA) 도입: 도메인별로 서비스를 분리하여 독립적인 배포와 확장이 가능하도록 구성합니다. 세부적인 서비스별 기술 표준 및 가이드는 docs/MICROSERVICE_COMMON_GUIDELINE.md를 따릅니다.
- JVM 메모리 최적화 전략: t4g.nano/micro와 같은 저사양 인프라에 맞게 모든 마이크로서비스의 JVM 힙 메모리(Heap), 스택 사이즈, 가비지 컬렉터(GC) 설정을 세밀하게 조정하여 메모리 효율성을 극대화합니다. 구체적인 가이드는 docs/spring-boot-memory-optimization-guide.md를 따릅니다.


## 4. 에이전트 작업 지침

* 향후 시스템 설계 및 구현 시, 레거시 시스템의 기능을 분석하여 MSA 원칙에 부합하도록 도메인 단위로 서비스를 분리하고 설계해야 합니다.
* 모든 인프라 변경사항은 반드시 IaC 코드로 작성되어 infrastructure 서브모듈에서 관리되어야 합니다.
* 시스템 아키텍처에 중대한 변경이 발생할 경우 이 문서를 최신 상태로 업데이트해야 합니다.
