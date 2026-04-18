# Last Work Summary (2026-04-18)

## 1. 개요
Next.js 15 기반의 새로운 프론트엔드 BFF(Backend for Frontend)인 microservice-frontend 모듈을 초기화하고 핵심 인증 및 프록시 로직을 구현하였습니다.

## 2. 수행 작업 내용

### 2.1 Next.js 15 프로젝트 초기화 및 UI 마이그레이션
- 프로젝트 구축: App Router와 TypeScript를 사용하는 Next.js 15 프로젝트를 microservice-frontend 디렉토리에 구축하고 레거시 UI를 완벽히 이관하였습니다.
- MUI v6 환경 설정: ThemeRegistry 및 EmotionCache를 구현하여 Next.js App Router 환경에서 Material UI v6가 정상 동작하도록 설정하였습니다.
- 라이브러리 도입: JWT 서명 및 검증을 위해 jose 라이브러리를 설치하고, API 통신을 위해 axios 및 react-router-dom(호환용)을 설치하였습니다.

### 2.2 세션 관리 및 인증 아키텍처 구현
- JWT 세션 시스템: HS256 알고리즘 기반의 JWT 발급 및 검증 로직을 src/lib/session.ts에 구현하였습니다. 세션은 qc_session 명칭의 httpOnly 쿠키로 관리됩니다.
- 네이버 OAuth2 연동: 로그인 시작, 콜백 수신 및 CSRF 방지용 state 검증 로직을 포함한 인증 라우트를 구현하였습니다.
- SSR 및 CSR 최적화: localStorage 접근 시 윈도우 객체 존재 여부를 체크하도록 수정하고, useSearchParams 사용 시 Suspense 바운더리를 적용하여 빌드 안정성을 확보하였습니다.

### 2.3 API 프록시 및 라우팅 이관
- 통합 프록시 라우트: 브라우저의 요청을 받아 세션에서 memberId를 추출하고, 이를 X-Member-Id 헤더에 주입하여 내부 API Gateway로 전달하는 프록시 로직을 구현하였습니다.
- 라우팅 구조 변경: React Router DOM 기반의 라우팅을 Next.js 파일 시스템 기반 라우팅(src/app)으로 전면 개편하였습니다.
- 링크 및 네비게이션 최적화: react-router-dom의 Link 및 useNavigate를 Next.js의 Link 및 useRouter/usePathname으로 교체하여 프레임워크 최적화를 완료하였습니다.

### 2.4 컨테이너화 및 배포 준비
- Docker 최적화: ARM64 아키텍처(AWS t4g.micro 등)에 최적화된 멀티스테이지 Dockerfile을 작성하였습니다.
- 빌드 검증: npm run build를 통해 전체 타입 체크 및 프로덕션 빌드 성공을 확인하였습니다.

## 3. 향후 과제
- API Gateway와의 실환경 통합 테스트 수행.
- 레거시 기능 중 미진한 세부 UI 인터랙션 검증.
- 마이크로서비스 간 분산 세션 공유 및 보안 고도화.

---

# Previous Work Summary (2026-04-06)

# Previous Work Summary (2026-04-04)

## 1. 개요
마이크로서비스 프로젝트의 포트 정책을 재정의하고, `microservice-customer` 서비스 구현 및 전체 모듈의 API 문서화 표준을 적용하였습니다. 또한 이벤트 기반 아키텍처(EDA) 설계 지침을 공통 가이드라인으로 통합하여 문서 관리 효율성을 높였습니다.

## 2. 수행 작업 내용

### 2.1 포트 정책 및 인프라 설정 변경
- 포트 단일화: 모든 마이크로서비스(`gateway`, `member`, `book`, `talk`, `customer`)의 컨테이너 내부 포트를 8080으로 통일하였습니다.
- 가이드라인 업데이트: `docs/MICROSERVICE_COMMON_GUIDELINE.md`에 포트 8080 사용 및 컨테이너 격리 원칙을 명시하였습니다.
- 루트 `README.md` 업데이트: 인프라 아키텍처 현황(Spring Cloud Gateway 도입 완료 등)을 최신화하였습니다.

### 2.2 microservice-customer 핵심 구현
- 헥사고날 아키텍처 적용: 도메인(`CustomerMessage`), 애플리케이션 서비스, 웹/지속성 어댑터를 구현하였습니다.
- 데이터베이스 설정: PostgreSQL 연결 정보 및 Flyway 초기 스크립트(`V1__init.sql`)를 추가하였습니다.
- 테스트 구축: `CustomerMessageServiceTest`를 통해 메시지 생성 로직의 단위 테스트를 완료하였습니다.

### 2.3 API 문서화 및 스펙 제공 표준화
- 모든 모듈(`member`, `book`, `customer`)에 Spring RestDocs 및 OpenAPI 3.0(restdocs-api-spec) 설정을 적용하였습니다. (기존 `talk` 모듈 표준 전파)
- `SpecController` 구현: 모든 서비스에 `/api/v1/spec` 엔드포인트를 추가하여 런타임에 최신 API 명세(YAML)를 제공하도록 하였습니다.

### 2.4 EDA 지침 통합 및 문서 관리 최적화
- EDA 가이드 통합: 독립되어 있던 `docs/EDA_GUIDELINE.md`의 내용을 `docs/MICROSERVICE_COMMON_GUIDELINE.md`의 '7. 서비스 간 통신' 섹션으로 통합하였습니다.
- 설계 표준 명문화: 트랜잭셔널 아웃박스 패턴, 도메인 중심 토픽 설계, 엔티티 기반 파티셔닝, 계층적 재시도 및 DLQ 전략을 공통 지침으로 확정하였습니다.
- 문서 단순화: 중복된 `EDA_GUIDELINE.md` 파일을 삭제하고, `README.md`, `ARCHITECTURE_VISION.md`, `BACKEND_DESIGN.md` 내의 참조 링크를 통합된 문서로 업데이트하였습니다.

### 2.6 가이드라인 통합 및 문서 최적화
- 핵심 문서 3종 체계 구축: 파편화되어 있던 7개의 가이드라인을 성격에 따라 `ARCHITECTURE.md` (설계/비전), `DEVELOPMENT.md` (코딩/기술 표준), `HISTORY.md` (이력)로 통합하였습니다.
- 정보 접근성 향상: 루트 `README.md`의 문서 참조 링크를 최신화하여 개발자와 AI 에이전트가 단일 진입점을 통해 정보를 파악할 수 있도록 개선하였습니다.
- AI 에이전트 작업 원칙 수립: DEVELOPMENT.md에 업계 표준 우선 선정 및 단일 최적안 제안 원칙을 명문화하여 AI의 효율적인 의사결정을 유도하였습니다.
- 문서 스타일 가이드 강화: 마크다운 작성 시 강조 서식(굵게, 기울임) 및 이모티콘 사용을 전면 금지하는 규칙을 DEVELOPMENT.md에 명문화하고 기존 문서에 적용하였습니다.
- 루트 AGENTS.md 생성: 프로젝트 진입 시 AI 에이전트가 읽어야 할 핵심 지침을 루트 디렉토리에 구축하고 README.md에서 이를 참조하도록 수정하였습니다.
- 요구사항 관리 체계 구축: docs/requirements 디렉토리를 생성하고 통합 제품 요구사항 정의서(prd.md)를 작성하여 프로젝트의 목표와 기능을 명문화하였습니다.
- 중복 제거 및 최신화: 각 문서에 흩어져 있던 서비스 목록, 기술 스택, 포트 정책 등을 통합하고 현재 구현 상태에 맞춰 내용을 갱신하였습니다.

## 3. 향후 과제
- Prometheus/Grafana를 활용한 서비스 메트릭 가시화.
- 각 서비스 간의 통신(RestClient + LoadBalancer) 로직 구체화 및 견고성 확보.
- 레거시 기능의 완전한 이관 및 통합 테스트 수행.

---

# Previous Work Summary (2026-04-04)
...
