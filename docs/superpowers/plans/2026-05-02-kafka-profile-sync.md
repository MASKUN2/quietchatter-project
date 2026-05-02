# Kafka 기반 프로필 변경 동기화 실행 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 사용자가 닉네임을 변경하면 `MemberProfileUpdatedEvent`를 발행하고, `Talk` 서비스가 이를 수신하여 해당 회원이 작성한 모든 Talk의 닉네임 스냅샷을 최신화합니다.

**Architecture:** 
- **Member**: Outbox 패턴을 활용한 `MemberProfileUpdatedEvent` 발행
- **Talk**: `MemberEventConsumer`에서 이벤트 수신 및 JPA Bulk Update를 통한 스냅샷 동기화

**Tech Stack:** Kotlin (Spring Boot), Spring Cloud Stream Kafka, Git

---

### Task 1: Member 서비스 프로필 변경 이벤트 발행

**Files:**
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/member/application/MemberService.kt`

- [ ] **Step 1: updateNickname 로직에 OutboxEvent 저장 추가**

```kotlin
    @Transactional
    fun updateNickname(id: UUID, nickname: String) {
        val member = memberRepository.findById(id).orElseThrow { 
            MemberNotFoundException("Member not found") 
        }
        member.updateNickname(nickname)

        // 이벤트 발행 로직 추가
        val eventPayload = """{"memberId": "${member.id}", "nickname": "${member.nickname}"}"""
        val outboxEvent = OutboxEvent(
            aggregateType = "Member",
            aggregateId = member.id.toString(),
            type = "MemberProfileUpdatedEvent",
            payload = eventPayload
        )
        outboxEventRepository.save(outboxEvent)
    }
```

- [ ] **Step 2: 빌드 확인 및 커밋**

```bash
cd microservice-member && ./gradlew build && git add . && git commit -m "feat(member): publish MemberProfileUpdatedEvent on nickname change"
```

---

### Task 2: Talk 서비스 스냅샷 업데이트 로직 구현

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/in/TalkCommandable.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/TalkService.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/out/TalkPersistable.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/out/persistence/TalkPersistenceAdapter.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/out/persistence/TalkJpaRepository.kt`

- [ ] **Step 1: TalkCommandable에 메서드 추가**

```kotlin
fun updateAuthorNickname(memberId: UUID, newNickname: String)
```

- [ ] **Step 2: TalkJpaRepository에 벌크 업데이트 쿼리 추가**

```kotlin
@Modifying
@Query("UPDATE Talk t SET t.nickname = :nickname WHERE t.memberId = :memberId")
fun updateNicknameByMemberId(memberId: UUID, nickname: String)
```

- [ ] **Step 3: 서비스 및 어댑터 구현**

- [ ] **Step 4: 빌드 확인 및 커밋**

```bash
cd microservice-talk && ./gradlew build && git add . && git commit -m "feat(talk): add bulk update for author nickname snapshot"
```

---

### Task 3: Talk 서비스 Kafka 소비자 확장

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/messaging/MemberEventConsumer.kt`

- [ ] **Step 1: MemberProfileUpdatedEvent 처리 분기 추가**

```kotlin
            if (eventType == "MemberProfileUpdatedEvent") {
                val memberId = UUID.fromString(eventDto.memberId!!)
                val nickname = eventDto.nickname!!
                log.info("Processing MemberProfileUpdatedEvent for memberId: {}, newNickname: {}", memberId, nickname)
                talkCommandable.updateAuthorNickname(memberId, nickname)
            }
```

- [ ] **Step 2: 빌드 확인 및 커밋**

```bash
cd microservice-talk && ./gradlew build && git add . && git commit -m "feat(talk): handle MemberProfileUpdatedEvent to sync snapshots"
```

---

### Task 4: 최종 검증 및 아카이브

- [ ] **Step 1: 전체 프로젝트 빌드 확인**

- [ ] **Step 2: 최종 커밋**

```bash
git add . && git commit -m "feat: complete Kafka-based profile sync for talk author info"
```
