# 설계 문서: Kafka를 활용한 프로필 변경 이벤트 전파 및 스냅샷 갱신

이 문서는 사용자가 닉네임을 변경했을 때, `microservice-member`에서 이벤트를 발행하고 `microservice-talk`에서 이를 구독하여 기존 Talk에 저장된 작성자 정보(닉네임 스냅샷)를 최신화하기 위한 설계를 기술합니다.

## 1. 개요 및 목적
*   **목적**: Talk 서비스가 자체적으로 유지하는 작성자 닉네임 스냅샷의 데이터 일관성 유지.
*   **방식**: Kafka를 통한 비동기 이벤트 전파 (Event-carried State Transfer).
*   **신뢰성**: 트랜잭션 아웃박스(Outbox) 패턴을 활용하여 메시지 발행 보장.

## 2. 컴포넌트 설계

### A. Member 서비스 (`microservice-member`)
*   **변경 지점**: `com.quietchatter.member.application.MemberService.updateNickname`
*   **작업 내용**:
    *   회원 엔티티의 닉네임 변경 성공 후, `OutboxEvent` 저장 로직 추가.
    *   **이벤트 타입**: `MemberProfileUpdatedEvent`
    *   **Payload**: `{"memberId": "{UUID}", "nickname": "{String}"}`

### B. Talk 서비스 (`microservice-talk`)
*   **Inbound Port**: `com.quietchatter.talk.application.in.TalkCommandable`
    *   `updateAuthorNickname(memberId: UUID, newNickname: String)` 메서드 추가.
*   **Application Service**: `com.quietchatter.talk.application.TalkService`
    *   포트 구현: 특정 `memberId`를 가진 모든 `Talk` 엔티티의 `nickname`을 새로운 값으로 업데이트.
    *   **성능 최적화**: JPA 벌크 업데이트(Query 어노테이션) 또는 영속성 컨텍스트를 고려한 업데이트 수행.
*   **Messaging Adaptor**: `com.quietchatter.talk.adaptor.in.messaging.MemberEventConsumer`
    *   `MemberProfileUpdatedEvent` 타입 수신 시 `talkCommandable.updateAuthorNickname` 호출.

## 3. 구현 단계
1.  **Member 서비스**: `updateNickname` 로직에 이벤트 발행 코드 추가 및 빌드 검증.
2.  **Talk 서비스**: 
    *   `TalkCommandable` 및 `TalkService`에 닉네임 업데이트 기능 추가.
    *   `MemberEventConsumer`에 이벤트 처리 분기 추가.
3.  **검증**: 닉네임 변경 시 실제 Talk 테이블의 데이터가 변경되는지 확인(통합 테스트 또는 DB 조회).

## 4. 즉시 커밋 전략
각 마이크로서비스별 수정 사항이 반영되고 빌드가 통과될 때마다 즉시 로컬 커밋을 수행합니다.
