# Migration from Avro to Flattened JSON Messaging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transition from Avro-based messaging to a flattened JSON format with `evt_` metadata prefixes, removing dependencies on Avro and Schema Registry.

**Architecture:** Use Spring Cloud Stream's default JSON support. Producers will flatten their `payload` and add `evt_` metadata. Consumers will use service-specific DTOs.

**Tech Stack:** Kotlin, Spring Boot, Spring Cloud Stream Kafka, Jackson.

---

### Task 1: Microservice-Member - Preparation & Dependency Removal

**Files:**
- Modify: `microservice-member/build.gradle.kts`

- [ ] **Step 1: Remove Avro plugin and dependencies**
Remove `com.github.davidmc24.gradle.plugin.avro`, `kafka-avro-serializer`, `avro`, and `avro { ... }` block.

- [ ] **Step 2: Refresh gradle and verify build**
Run: `./gradlew clean build -x test` in `microservice-member`
Expected: Build success (though some code will now have compilation errors).

- [ ] **Step 3: Commit**
```bash
git add microservice-member/build.gradle.kts
git commit -m "chore(member): remove avro dependencies"
```

---

### Task 2: Microservice-Member - Integration Event DTO & Producer Update

**Files:**
- Create: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/out/messaging/MemberIntegrationEvent.kt`
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/out/outbox/OutboxRelayService.kt`
- Modify: `microservice-member/src/main/resources/application.yml`

- [ ] **Step 1: Create MemberIntegrationEvent DTO**
This DTO will represent the flattened JSON structure.

```kotlin
package com.quietchatter.member.adaptor.out.messaging

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonProperty

data class MemberIntegrationEvent(
    @JsonProperty("evt_id") val evtId: String,
    @JsonProperty("evt_agg_id") val evtAggId: String,
    @JsonProperty("evt_type") val evtType: String,
    @JsonProperty("evt_time") val evtTime: String,
    @JsonAnyGetter val payload: Map<String, Any?>
)
```

- [ ] **Step 2: Update OutboxRelayService to use JSON DTO**
Update `relayEvents` to parse the payload and build `MemberIntegrationEvent`.

```kotlin
// ... inside relayEvents loop
val payloadMap = objectMapper.readValue(event.payload, Map::class.java) as Map<String, Any?>
val integrationEvent = MemberIntegrationEvent(
    evtId = event.id.toString(),
    evtAggId = event.aggregateId,
    evtType = event.type,
    evtTime = event.createdAt.toString(),
    payload = payloadMap
)

val message = MessageBuilder.withPayload(integrationEvent)
    .setHeader(KafkaHeaders.KEY, event.aggregateId.toByteArray())
    .build()
// ...
```

- [ ] **Step 3: Update application.yml content-type**
Change `content-type` from `application/*+avro` to `application/json`. Remove any schema registry config if present.

- [ ] **Step 4: Verify with a test**
Create or update a test to verify JSON serialization of the event.

- [ ] **Step 5: Commit**
```bash
git add microservice-member/src/main/kotlin microservice-member/src/main/resources
git commit -m "feat(member): migrate outbox relay to flattened JSON"
```

---

### Task 3: Microservice-Talk - Preparation & Consumer Update

**Files:**
- Modify: `microservice-talk/build.gradle.kts`
- Create: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/messaging/MemberEventDto.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/messaging/MemberEventConsumer.kt`
- Modify: `microservice-talk/src/main/resources/application.yml`

- [ ] **Step 1: Remove Avro dependencies from microservice-talk**
Same as Task 1 Step 1.

- [ ] **Step 2: Create MemberEventDto for consumption**
Only include fields needed by Talk service.

```kotlin
package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MemberEventDto(
    @JsonProperty("evt_type") val evtType: String,
    @JsonProperty("memberId") val memberId: String? = null
)
```

- [ ] **Step 3: Update MemberEventConsumer to use JSON DTO**
```kotlin
@Bean
fun memberEvents(): Consumer<Message<MemberEventDto>> {
    return Consumer { message ->
        val event = message.payload
        log.debug("Received member event: {}", event.evtType)

        if (event.evtType == "MemberDeactivatedEvent") {
            event.memberId?.let {
                val memberId = UUID.fromString(it)
                talkCommandable.hideAllByMember(memberId)
            }
        }
    }
}
```

- [ ] **Step 4: Update application.yml for Talk service**
Change `content-type` to `application/json` for `memberEvents-in-0` and `talkEvents-out-0`.

- [ ] **Step 5: Commit**
```bash
git add microservice-talk
git commit -m "feat(talk): migrate member event consumer to JSON"
```

---

### Task 4: Microservice-Talk - Producer Update

**Files:**
- Create: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/out/messaging/TalkIntegrationEvent.kt`
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/out/outbox/OutboxRelayService.kt`

- [ ] **Step 1: Create TalkIntegrationEvent DTO**
- [ ] **Step 2: Update OutboxRelayService for Talk events**
- [ ] **Step 3: Commit**

---

### Task 5: Microservice-Book - Migration

**Files:**
- Modify: `microservice-book/build.gradle.kts`
- Modify: `microservice-book/src/main/resources/application.yml`
- Create: `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/messaging/BookIntegrationEvent.kt`
- Modify: `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/outbox/OutboxRelayService.kt`

- [ ] **Step 1: Remove Avro dependencies from microservice-book**
- [ ] **Step 2: Create BookIntegrationEvent and update OutboxRelayService**
- [ ] **Step 3: Update application.yml**
- [ ] **Step 4: Commit**

---

### Task 6: Global Cleanup & Documentation

- [ ] **Step 1: Delete all .avsc files**
Remove `src/main/avro/*.avsc` from all services.

- [ ] **Step 2: Update docs/DEVELOPMENT.md and docs/ARCHITECTURE.md**
Update messaging sections to reflect JSON usage and `evt_` naming convention.

- [ ] **Step 3: Final check of infrastructure/redpanda.yaml**
Remove schema registry if it's no longer needed (optional, depending on other uses).

- [ ] **Step 4: Commit cleanup**
```bash
git commit -m "chore: final cleanup of avro files and documentation"
```
