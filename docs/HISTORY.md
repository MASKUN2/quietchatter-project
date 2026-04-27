# Last Work Summary (2026-04-27)

## 1. 개요
`microservice-customer` 서비스를 `microservice-member` 서비스로 통합하여 단일 서비스로 운영하고, 시스템 복잡도를 낮추었습니다.

## 2. 수행 작업 내용

### 2.1 마이크로서비스 통합 (Member + Customer)
- 코드 마이그레이션: `microservice-customer`의 도메인, 애플리케이션, 어댑터 레이어 코드를 `microservice-member`의 `com.quietchatter.customer` 패키지로 이관하였습니다.
- ID 체계 표준화: `CustomerMessage` 엔티티의 ID를 기존 `Long`에서 `microservice-member` 표준인 `UUID`로 변경하고, 공통 `BaseEntity`를 상속받도록 리팩토링하였습니다.
- 예약어 충돌 해결: Kotlin 예약어인 `in` 패키지 명칭을 `port_in` 및 `port_out`으로 변경하여 컴파일 오류를 해결하고 명확성을 높였습니다.

### 2.2 인프라 및 라우팅 설정 갱신
- API Gateway 설정 업데이트: `microservice-api-gateway`의 라우팅 설정을 수정하여 `/v1/customer/**` 경로의 요청이 `member-service`로 전달되도록 변경하였습니다.
- 데이터베이스 마이그레이션: `microservice-member`에 고객 문의 테이블 생성을 위한 Flyway 마이그레이션 파일(`V3__create_customer_message_table.sql`)을 추가하였습니다.
- 빌드 최적화: `microservice-member`의 `build.gradle.kts`를 수정하여 Avro Java 코드 생성 태스크(`generateAvroJava`)와 Kotlin 컴파일 태스크 간의 의존성 관계를 명확히 정의하였습니다.

### 2.3 프로젝트 구조 정리
- 레거시 모듈 삭제: 통합이 완료된 `microservice-customer` 디렉토리와 관련 서브모듈 설정을 제거하였습니다.

## 3. 향후 과제
- 통합된 `member-service` 내의 고객 문의 기능 엔드투엔드(E2E) 테스트 수행.
- 서비스 통합에 따른 리소스 사용량 변화 모니터링.

---

# Previous Work Summary (2026-04-19)

## 1. 개요
Terraform 기반 인프라를 리팩토링하여 설정 파일의 S3 동적 동기화 구조를 구축하고, 네트워크 가용성 및 보안성 향상을 위해 NAT와 Ingress 노드를 분리하였습니다.

## 2. 수행 작업 내용

### 2.1 공통 인프라 에셋 관리 체계 구축
- 공통 S3 버킷 생성: 01-base 레이어에 quietchatter-infra-assets 명칭의 공통 버킷을 생성하여 모든 노드의 설정 파일과 스크립트를 통합 관리하도록 하였습니다.
- 동적 동기화 구현: 범용 sync.sh 스크립트와 systemd timer를 모든 노드(NAT, Ingress, Controlplane)에 적용하여 S3로부터 최신 설정(docker-compose, nginx.conf, config.alloy)을 5분 주기로 자동 반영하는 구조를 확립하였습니다.
- 시크릿 주입 고도화: Secrets Manager의 태그(controlplane=true) 기반 자동 탐색 로직을 sync.sh에 통합하여 시크릿 변경 시 환경변수 및 Alloy 설정이 즉시 갱신되도록 하였습니다.

### 2.2 NAT 및 Ingress 노드 역할 분리
- 전용 NAT 노드 구축: t4g.nano 사양의 전용 인스턴스를 01-base 레이어에 구축하여 프라이빗 서브넷의 순수 인터넷 게이트웨이 역할을 수행하도록 고정하였습니다.
- 단독 Ingress 노드 구축: t4g.micro 사양의 단독 인스턴스를 02-network-services 레이어에 배치하여 외부 트래픽 수용 및 라우팅 전담 체계를 마련하였습니다.
- 보안 그룹 리팩토링: nat-sg(내부 전용)와 ingress-sg(80/443 외부 개방)를 명확히 분리하여 보안 계층을 강화하였습니다.

### 2.3 Consul DNS 기반 동적 라우팅 적용
- Consul Client 실행: Ingress 노드 내부에 Consul Client(Agent)를 배치하여 로컬 DNS 서버(127.0.0.1:8600) 역할을 수행하도록 하였습니다.
- Nginx 설정 최적화: 하드코딩된 IP 대신 Consul DNS 도메인(service.consul)을 사용하도록 nginx.conf를 리팩토링하였습니다.
- 런타임 가용성 확보: Nginx 변수와 resolver 설정을 결합하여 시작 시 대상 서비스가 없더라도 프로세스가 중단되지 않고 런타임에 이름을 해석하도록 구현하였습니다.

### 2.4 인프라 코드 및 설정 최적화
- Terraform 정리: 03-platform의 인라인 locals 설정을 제거하고 S3 remote_state 참조 방식으로 일원화하였습니다. 미사용 변수(api_gateway_image 등)와 템플릿 파일들을 정리하여 코드 가독성을 높였습니다.
- 부트스트랩 리팩토링: user_data 스크립트를 최소한의 패키지 설치 및 sync.sh 실행 위주의 부트스트랩 로직으로 경량화하였습니다.

## 3. 향후 과제
- 마이크로서비스 서비스 등록 및 Consul DNS 연동 최종 통합 테스트.
- Ingress 노드의 SSL(HTTPS) 인증서 적용 및 자동 갱신 체계 마련.
- 시스템 메트릭(Alloy/Loki) 가시성 확보 및 대시보드 구축.

---

# Previous Work Summary (2026-04-18)

## 1. 개요
Next.js 15 기반의 새로운 프론트엔드 BFF(Backend for Frontend)인 microservice-frontend 모듈을 초기화하고 핵심 인증 및 프록시 로직을 구현하였습니다.

## 2. 수행 작업 내용

### 2.1 Next.js 15 프로젝트 초기화 및 UI 마이그레이션
- 프로젝트 구축: App Router와 TypeScript를 사용하는 Next.js 15 프로젝트를 microservice-frontend 디렉토리에 구축하고 레거시 UI를 완벽히 이관하였습니다.
- MUI v6 환경 설정: ThemeRegistry 및 EmotionCache를 구현하여 Next.js App Router 환경에서 Material UI v6가 정상 동작하도록 설정하였습니다.
- 라이브러리 도입: JWT 서명 및 검증을 위해 jose 라이브러리를 설치하고, API 통 신을 위해 axios 및 react-router-dom(호환용)을 설치하였습니다.

### 2.2 세션 관리 및 인증 아키텍처 구현
- JWT 세션 시스템: HS256 알고리즘 기반의 JWT 발급 및 검증 로직을 src/lib/session.ts에 구현하였습니다. 세션은 qc_session 명칭의 httpOnly 쿠키로 관리됩니다.
- 네이버 OAuth2 연동: 로그인 시작, 콜백 수신 및 CSRF 방지용 state 검증 로직을 포함한 인증 라우트를 구현하였습니다.
- SSR 및 CSR 최적화: localStorage 접근 시 윈도우 객체 존재 여부를 체크하도록  수정하고, useSearchParams 사용 시 Suspense 바운더리를 적용하여 빌드 안정성을  확보하였습니다.

### 2.3 API 프록시 및 라우팅 이관
- 통합 프록시 라우트: 브라우저의 요청을 받아 세션에서 memberId를 추출하고, 이 를 X-Member-Id 헤더에 주입하여 내부 API Gateway로 전달하는 프록시 로직을 구현 하였습니다.
- 라우팅 구조 변경: React Router DOM 기반의 라우팅을 Next.js 파일 시스템 기반 라우팅(src/app)으로 전면 개편하였습니다.
- 링크 및 네비게이션 최적화: react-router-dom의 Link 및 useNavigate를 Next.js 의 Link 및 useRouter/usePathname으로 교체하여 프레임워크 최적화를 완료하였습니다.

### 2.4 컨테이너화 및 배포 준비
- Docker 최적화: ARM64 아키텍처(AWS t4g.micro 등)에 최적화된 멀티스테이지 Dockerfile을 작성하였습니다.
- 빌드 검증: npm run build를 통해 전체 타입 체크 및 프로덕션 빌드 성공을 확인 하였습니다.

## 3. 향후 과제
- API Gateway와의 실환경 통합 테스트 수행.
- 레거시 기능 중 미진한 세부 UI 인터랙션 검증.
- 마이크로서비스 간 분산 세션 공유 및 보안 고도화.

---

# Previous Work Summary (2026-04-06)
...
