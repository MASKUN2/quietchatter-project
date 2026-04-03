# 백엔드 마이크로서비스 설계 및 전환 계획 (Backend MSA Design & Migration Plan)

본 문서는 레거시 시스템(legacy-quiet-chatter 등)을 코틀린(Kotlin) 및 최신 스프링 부트(Spring Boot) 기반의 마이크로서비스로 전환하기 위한 구체적인 아키텍처 설계와 모듈 분리 전략을 정의합니다.

## 1. 기술 스택 선정 및 버저닝 전략
### 1.1 언어 및 프레임워크
* 언어: Kotlin (코루틴을 활용한 비동기 처리 및 Null 안정성 확보)
* 프레임워크: Spring Boot 3.5.x 최신 안정화 버전 (예: 3.5.13) + Spring Cloud (2025.0.x)
* 메모리 최적화 전략 (JVM Tuning):
  * t4g.nano/micro 환경을 고려하여 마이크로서비스별 최대 힙 메모리(-Xmx)를 128MB~256MB로 엄격히 제한합니다.
  * 스레드 스택 사이즈(-Xss)를 기본 1MB에서 256KB로 축소하여 메모리 점유율을 낮춥니다.
  * 저사양 환경에 최적화된 Serial GC를 적극 검토하여 불필요한 GC 오버헤드를 줄입니다.
  * 계층형 컴파일(Tiered Compilation) 및 웜업 최적화를 통해 리소스 사용 효율을 높입니다.

### 1.2 Spring Boot 버전에 대한 판단 (Spring Boot 3 확정)
...

* AI 에이전트 관점의 판단: Spring Boot 4 버전은 AI 에이전트의 학습 데이터가 상대적으로 부족하거나 최신 API에 대한 할루시네이션(환각)을 유발할 위험이 높습니다. 반면, Spring Boot 3.x는 릴리스 이후 오랜 기간 안정화되었으며 AI 에이전트가 코틀린과 결합된 풍부하고 정확한 레퍼런스를 보유하고 있습니다.
* 결론: 개발 생산성과 AI 에이전트의 코드 생성 정확도를 극대화하기 위해, 이 프로젝트는 **Spring Boot 3 최신 버전(3.4.x)**을 표준으로 사용합니다.

## 2. 인증 및 권한 관리 전략 (Authentication & Authorization)

MSA 환경에서 보안의 복잡성을 줄이고 각 서비스의 독립성을 보장하기 위해 다음과 같은 전략을 사용합니다.

### 2.1 게이트웨이 기반 JWT 검증 및 헤더 삽입 (Header Insertion)
* 클라이언트 요청 처리: 모든 클라이언트는 게이트웨이를 통해 요청을 보내며, 이때 Authorization 헤더에 JWT(JSON Web Token)를 포함합니다.
* 게이트웨이의 역할 (Spring Cloud Gateway):
  * JWT 토큰의 유효성(서명, 만료 시간 등)을 중앙에서 단 한 번 검증합니다.
  * 유효한 토큰일 경우, 페이로드에서 사용자 식별 정보(User ID, Role 등)를 추출합니다.
  * 추출된 정보를 커스텀 HTTP 헤더에 삽입하여 내부 마이크로서비스로 전달합니다. (예: `X-User-Id`, `X-User-Role`)
* 내부 마이크로서비스의 역할:
  * 별도의 복잡한 JWT 검증 로직을 구현하지 않습니다.
  * 게이트웨이가 전달한 HTTP 헤더 정보를 전적으로 신뢰하여 비즈니스 로직을 수행합니다.
  * 내부망(Private Subnet) 보안 그룹 설정을 통해 게이트웨이 이외의 직접적인 접근을 차단하여 신뢰성을 확보합니다.


기존 레거시 패키지 구조(maskun.quietchatter.*)를 분석한 결과를 바탕으로, 기능의 응집도와 배포의 독립성을 고려하여 다음과 같이 서비스를 분할합니다.

| 서비스 (서브모듈 이름) | 주요 도메인 (기존 레거시 패키지) | 핵심 책임 및 역할 |
| :--- | :--- | :--- |
| microservice-user | member, security | 회원 가입, 인증/인가(OAuth), 사용자 프로필 및 권한 관리 |
| microservice-book | book | 책 정보 제공, 검색, 큐레이션 및 메타데이터 관리 |
| microservice-talk | talk, reaction | 사용자 간의 대화, 피드백(반응), 커뮤니케이션 도메인 처리 |
| microservice-customer| customer | 고객 센터, 고객 문의 메시지 접수 및 CS 처리 |

(참고: 추후 비즈니스 확장에 따라 알림 등의 추가 서비스 분리가 필요할 수 있습니다.)

## 3. 게이트웨이 및 관리 노드 구성 계획

마이크로서비스들을 하나로 묶고 통제하기 위해 다음과 같이 게이트웨이와 관리 계층을 구성합니다.

### 3.1 API Gateway 구성 (microservice-gateway - 신규 필요)
* 기술 스택: Spring Cloud Gateway (비동기 논블로킹 기반의 WebFlux 활용)
* 주요 역할:
  * 단일 진입점(Ingress): 외부의 모든 클라이언트 요청을 받아 적절한 내부 마이크로서비스로 라우팅합니다.
  * 인증 및 인가 처리: JWT 토큰 검증 등을 게이트웨이 단에서 공통으로 처리하여 내부 서비스의 부담을 줄입니다.
  * 보안 및 트래픽 제어: Rate Limiting(초당 요청 수 제한), CORS 처리, 로깅 등을 담당합니다.
* 통신: Eureka 서버를 참조하여 동적으로 할당되는 내부 마이크로서비스의 주소(IP/Port)를 자동으로 찾아 라우팅합니다.

### 3.2 서비스 탐색 및 설정 관리 (Consul)
별도의 관리용 마이크로서비스 앱을 개발하는 대신, 인프라 계층의 HashiCorp Consul을 활용하여 시스템을 통제합니다.
* HashiCorp Consul (핵심 인프라): 
  * 서비스 탐색: 모든 마이크로서비스는 구동 시 Consul에 자신을 등록하며, 게이트웨이는 이를 참조하여 동적으로 요청을 라우팅합니다.
  * 설정 관리: 각 서비스의 환경 설정값은 Consul의 Key-Value 저장소에서 중앙 집중식으로 관리하며 실시간 업데이트를 지원합니다.
  * 헬스 체크: 인프라 수준에서 서비스의 생존 여부를 주기적으로 확인합니다.


## 4. 향후 작업 순서 (Action Items)

1. 저장소 생성: quietchatter-microservice-gateway, quietchatter-microservice-talk, quietchatter-microservice-customer 저장소 생성 및 서브모듈 추가.
2. Management 서비스 구현: microservice-management에 Eureka, Config Server 구현 및 테스트.
3. Gateway 서비스 구현: microservice-gateway에 Spring Cloud Gateway 구현 및 라우팅 테스트.
4. 기반 서비스 포팅: 레거시의 회원 가입 및 인증 로직을 microservice-user로 포팅(Kotlin + Spring Boot 3).
