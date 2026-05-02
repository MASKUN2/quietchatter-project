# 설계 문서: 내부 API 기반 Talk 작성자 정보 스냅샷 저장

이 문서는 `microservice-talk`에서 Talk 작성 시 `microservice-member`로부터 작성자 정보(닉네임 등)를 내부 API를 통해 동기적으로 조회하고, 이를 `Talk` 엔티티에 스냅샷으로 저장하기 위한 설계를 기술합니다.

## 1. 주요 변경 및 요구 사항
*   **보안 및 경로**: 내부 서비스 간 통신을 위해 `/internal/**` 경로를 사용하며, 외부(API Gateway)에서의 접근은 차단됩니다.
*   **헤더 관리**:
    *   `X-Member-Nickname` 헤더 제거. `X-Member-Id`만 사용.
    *   내부 통신용 보안 헤더 (`X-Internal-Secret`) 추가.
    *   기존 API 버전 헤더 유지.
*   **서비스 간 통신**: Spring Cloud OpenFeign을 사용하며, k8s DNS(`service.namespace.svc.cluster.local`)를 통해 연결합니다.
*   **스냅샷 저장**: 조회된 닉네임을 `Talk` 엔티티의 `nickname` 필드에 저장하여 읽기 성능을 최적화하고 서비스 간 결합도를 낮춥니다.

## 2. 컴포넌트 설계

### A. Member 서비스 (`microservice-member`)
*   **Internal API**: `com.quietchatter.member.adaptor.in.web.internal.MemberInternalController`
    *   `GET /internal/api/members/{memberId}`: 특정 회원의 공개 정보(닉네임 등) 반환.
*   **DTO**: `InternalMemberResponse(id, nickname)`

### B. Talk 서비스 (`microservice-talk`)
*   **Feign Client**: `com.quietchatter.talk.adaptor.out.external.MemberClient`
    *   `@FeignClient`를 사용하여 Member 서비스의 internal API 호출.
    *   URL 설정 시 k8s 서비스 명칭 사용 (예: `http://microservice-member:8083`).
*   **Application 로직**: `TalkService.createTalk`
    1. `memberId`로 `MemberClient` 호출.
    2. 반환된 닉네임을 사용하여 `Talk` 엔티티 생성 및 저장.

### C. API Gateway (`microservice-api-gateway`)
*   **보안 필터**: `/internal/**` 경로로 들어오는 외부 요청을 `403 Forbidden` 또는 `404 Not Found`로 차단하도록 설정 추가.

## 3. 구현 단계
1.  **Member 서비스**: Internal 컨트롤러 및 DTO 구현.
2.  **Talk 서비스**: Feign 클라이언트 설정 및 서비스 로직 수정.
3.  **API Gateway**: 내부 경로 차단 로직 추가.
4.  **검증**: 통합 테스트 또는 빌드 확인.

## 4. 즉시 커밋 전략
각 마이크로서비스별 수정 및 설정 단계가 완료될 때마다 즉시 로컬 커밋을 수행합니다.
