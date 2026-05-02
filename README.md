# QuietChatter

내향적인 독자들을 위한 저자극 도서 공유 SNS 플랫폼입니다. AI 에이전트를 위한 작업 지침은 CLAUDE.md를 읽으십시오.

---

## 1. 제품 정의

슬로건: "You Belong Here"

자신을 드러내기 쑥스러워하는 사람들도 편안하게 책에 대한 이야기를 나누고, 조용히 공감할 수 있는 공간을 제공합니다.

핵심 가치:
- 익명성: 개인정보를 최소화하고, 부담 없이 생각을 표현할 수 있다.
- 휘발성: 일정 시간(기본 1년)이 지나면 글이 자동으로 숨겨진다.
- 저자극: 댓글/멘션 없이 단순한 공감 표현(좋아요, 공감해요)만 제공한다.

사용자 역할:
- 비회원: 도서 검색, 북톡 읽기 가능. 쓰기 활동 불가.
- 회원: 네이버 OAuth 로그인. 북톡 작성/수정/삭제, 반응 가능.

계정 상태: 활성(Active) / 탈퇴(Deactivated). 탈퇴 회원 재로그인 시 2시간 유효한 재활성화 토큰 발행.

---

## 2. 주요 기능

북톡(Talk): 특정 도서에 대해 최대 250자 감상평 작성. 삭제는 Soft Delete. 최신순 페이지네이션.

자동 숨김 정책: 작성 후 1년(기본값) 경과 시 자동 숨김. 사용자가 3개월/6개월/1년/3년/5년/10년 선택 가능. 매일 0시 기준 처리.

반응(Reaction): 좋아요(LIKE), 공감해요(SUPPORT) 두 가지. 낙관적 업데이트(Optimistic Update) 패턴 적용.

도서 정보: 네이버 도서 검색 API 연동. 검색 결과 무한 스크롤. 로컬 DB 캐싱.

고객 지원: VoC 버튼을 통해 모달 형태로 문의 메시지 접수.

닉네임 정책: 1~12자. 한글/영문/숫자 허용. 공백과 특수문자는 문자열 중간에만 허용.

---

## 3. 프로젝트 구조

모놀리식 레거시를 MSA로 전환하는 프로젝트입니다. 각 마이크로서비스는 독립된 Git 서브모듈로 관리됩니다.

```
quietchatter-project/
  microservice-api-gateway/   단일 진입점. JWT 검증 및 라우팅
  microservice-member/        회원 인증(OAuth), JWT 발급, 고객 문의
  microservice-book/          외부 도서 API 연동 및 캐싱
  microservice-talk/          북톡(대화), 반응 처리 및 자동화
  infrastructure/             Terraform 기반 AWS 인프라 (k3s 클러스터)
  legacy-*/                   참조용 아카이브 (수정 금지)
```

legacy-로 시작하는 모든 서브모듈은 보관(Archive) 상태입니다. 마이크로서비스 포팅 시 비즈니스 로직 참조용으로만 사용하며, 파일 수정 및 커밋은 절대 금지합니다.

---

## 4. 아키텍처 설계 원칙

### 인증 흐름

Gateway에서 JWT를 중앙 검증합니다. 검증된 회원 ID를 X-Member-Id 헤더에 삽입하여 내부 서비스로 전파합니다. 내부 서비스는 이 헤더를 신뢰합니다.

### 데이터 격리

서비스 간 DB 교차 참조 절대 금지. 각 서비스는 독립 논리 DB를 사용합니다.

### 서비스 간 통신

동기: Spring RestClient + k8s DNS(service.namespace.svc.cluster.local)
비동기: Redpanda(Kafka 호환) + Spring Cloud Stream. 이벤트 발행은 반드시 트랜잭셔널 아웃박스 패턴 적용.

메시지 규칙:
- 토픽명: {도메인명} 형식 (예: member). 세부 이벤트 타입은 메시지 본문의 evt_type 필드에 명시.
- 직렬화: 평면화된 JSON(Flattened JSON). 메타데이터 필드에 evt_ 접두사 사용.
- 메시지 키: 엔티티 ID 사용 (순서 보장).
- 최종 실패 시 {토픽명}.dlq로 격리.

### 헥사고날 아키텍처 패키지 구조

Gateway를 제외한 모든 서비스가 따르는 구조입니다.

```
com.quietchatter.{service}/
  domain/           순수 비즈니스 로직 (Entity, Value Object)
  application/
    in/             유스케이스 Port 인터페이스
    out/            외부 연동 Port 인터페이스
  adaptor/
    in/web/         RestController, DTO
    in/messaging/   Kafka Consumer
    in/scheduler/   스케줄러
    out/persistence/ JPA Repository 구현체
    out/outbox/     트랜잭셔널 아웃박스 구현체
    out/external/   외부 API 클라이언트
  config/           설정 분리
```

어댑터 패키지명은 반드시 adaptor(adapter 아님)를 사용합니다.

---

## 5. 기술 스택 및 개발 규칙

기술 스택:
- Backend: Kotlin + Spring Boot 3 + JDK 21 Virtual Threads
- API Gateway: Spring Cloud Gateway MVC (WebFlux/Reactive 사용 금지)
- Messaging: Redpanda + Spring Cloud Stream
- Database: PostgreSQL (서비스별 독립 DB) + Redis
- IaC: Terraform + AWS (k3s on EC2)
- CI/CD: GitHub Actions + Docker Hub + S3 Bridge

서비스 포트 (SERVER_PORT 환경변수로 설정):
- api-gateway: 8080
- microservice-member: 8083
- microservice-book: 8081
- microservice-talk: 8084

Kotlin 규칙:
- data class 사용, Lombok 금지.
- !! 사용 지양. 안전 연산자(?., ?:) 사용.
- 패키지명은 소문자 + 케밥케이스 조합.

API 문서화:
- Spring RestDocs + restdocs-api-spec으로 테스트 통과 시에만 문서 생성.
- 문서 생성 테스트에는 @Tag("restdocs") 적용.
- 각 서비스는 /api/v1/spec 엔드포인트로 최신 OpenAPI YAML 스펙 제공.

---

## 6. API 경로 표준

모든 요청은 /api 접두사로 시작합니다. URL에 버전 정보를 포함하지 않습니다.

| 리소스 | 담당 서비스 | 경로 패턴 |
| --- | --- | --- |
| 인증 | microservice-member | /api/auth/** |
| 회원 | microservice-member | /api/members/** |
| 고객지원 | microservice-member | /api/support/** |
| 도서 | microservice-book | /api/books/** |
| 북톡 | microservice-talk | /api/talks/** |
| 반응 | microservice-talk | /api/reactions/** |

---

## 7. 인프라 구성

AWS 서울 리전(ap-northeast-2) k3s 클러스터. 3노드 구성.

- controlplane (t4g.small): k3s server, Redis, Redpanda
- gateway (t4g.micro, 퍼블릭, EIP): NGINX, api-gateway
- worker ASG (t4g.small, Spot, min=1/max=3): member, book, talk

Terraform 계층 실행 순서: 01-base → 02-platform → 03-apps

Rolling Update 전략: 단일 Worker 노드 환경이므로 maxSurge: 0, maxUnavailable: 1 적용 필수.

자세한 인프라 작업 지침은 infrastructure/INFRASTRUCTURE.md를 읽으십시오.

---

## 8. 배포 방식 (S3 Bridge)

각 서비스의 k8s/deployment.yaml 템플릿(IMAGE_PLACEHOLDER 포함)이 매니페스트 구조의 원본입니다.

배포 흐름:
1. main 브랜치에 푸시 → GitHub Actions 트리거.
2. Docker 이미지 빌드 → Docker Hub에 sha-{short_sha} 태그로 푸시.
3. k8s/deployment.yaml의 IMAGE_PLACEHOLDER를 sha-{short_sha}로 치환.
4. 치환된 매니페스트를 S3(s3://quietchatter-infra-assets/controlplane/manifests/)에 업로드.
5. Controlplane의 sync.sh가 5분 주기로 S3에서 매니페스트를 내려받아 kubectl apply로 반영.

규칙: S3를 직접 수정한 경우(긴급 패치 등), 반드시 서비스 서브모듈 k8s/deployment.yaml에도 반영하고 커밋해야 합니다. 그렇지 않으면 다음 CI/CD 실행 시 변경이 롤백됩니다.

---

## 9. 작업 이력

### 2026-04-29

인프라 및 서비스 서브모듈 문서 정비. 문서 구조를 README.md / CLAUDE.md / GEMINI.md 3-파일 패턴으로 표준화.

Flyway 마이그레이션 결함 수정:
- microservice-member V4: nickname, role, status, provider, created_at, last_modified_at NOT NULL 추가.
- microservice-book V3: title, created_at, last_modified_at NOT NULL 추가.

k8s 매니페스트 불일치 해소:
- S3에 직접 적용한 Rolling Update 전략이 서비스 서브모듈 템플릿에 누락되어 있던 문제 발견. 4개 서비스 k8s/deployment.yaml에 strategy 블록 추가.
- scripts/sync.sh에서 LOKI_URL/LOKI_USER 조회 누락 및 금지 패턴(|| echo "") 발견, S3 원본과 동기화.

### 2026-04-28

인프라 운영 이슈 수정:
- Loki 시크릿(LOKI_URL, LOKI_USER)을 Secrets Manager에 등록하고 sync.sh에서 런타임 조회로 변경.
- Rolling Update 전략 수정: maxSurge=1(기본값) → maxSurge=0, maxUnavailable=1. 단일 Worker 노드에서 업데이트 Pending 문제 해결.
- Ghost Node 처리: Spot 인스턴스 종료 후 k3s 노드 레코드 미삭제 문제 확인. 수동 kubectl delete node 처리.

### 2026-04-27

microservice-customer를 microservice-member로 통합. 시스템 복잡도 감소.
- CustomerMessage 도메인 및 어댑터를 member 서비스로 이관.
- API Gateway 라우팅 업데이트: /api/support/** → member-service.
- Flyway V3 추가: customer_message 테이블 생성.

### 2026-04-26

k3s 전환: Consul + Docker Compose 기반 → k3s 단일 클러스터 전환.
Frontend 노드 제거: microservice-frontend 인프라 자원 전면 삭제.
인프라 레이어 통합: 6계층 → 3계층(01-base, 02-platform, 03-apps).

### 이전 주요 이력

- 2026-04-19: S3 Bridge 배포 구조 확립. user_data 방식 → systemd 타이머 + sync.sh + S3.
- 2026-04-18: microservice-frontend(Next.js 15 BFF) 초기화. (현재 제거됨)
- 레거시 포팅 완료: 핵심 비즈니스 로직을 Kotlin MSA로 재작성.
