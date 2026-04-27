# Book Microservice JSON Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate microservice-book from Avro messaging to JSON messaging with flattened structure and `evt_` prefixes.

**Architecture:** Replace Avro-based event serialization with a JSON-based integration event model. Use `ObjectMapper` to transform stored outbox payloads into a map for flattened JSON output.

**Tech Stack:** Kotlin, Spring Cloud Stream Kafka, Jackson.

---

### Task 1: Remove Avro Dependencies

**Files:**
- Modify: `microservice-book/build.gradle.kts`

- [ ] **Step 1: Remove Avro plugin and dependencies**

Remove:
- `id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"`
- `implementation("io.confluent:kafka-avro-serializer:7.5.0")`
- `implementation("org.apache.avro:avro:1.11.3")`
- `avro { ... }` configuration block.
- `maven { url = uri("https://packages.confluent.io/maven/") }` repository.

- [ ] **Step 2: Commit**

```bash
git add microservice-book/build.gradle.kts
git commit -m "chore(book): remove avro dependencies"
```

### Task 2: Create JSON Integration Event Model

**Files:**
- Create: `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/messaging/BookIntegrationEvent.kt`

- [ ] **Step 1: Create the directory**

```bash
mkdir -p microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/messaging
```

- [ ] **Step 2: Create BookIntegrationEvent.kt**

```kotlin
package com.quietchatter.book.adaptor.out.messaging

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonProperty

data class BookIntegrationEvent(
    @JsonProperty("evt_id") val evtId: String,
    @JsonProperty("evt_agg_id") val evtAggId: String,
    @JsonProperty("evt_type") val evtType: String,
    @JsonProperty("evt_time") val evtTime: String,
    @JsonAnyGetter val payload: Map<String, Any?>
)
```

- [ ] **Step 3: Commit**

```bash
git add microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/messaging/BookIntegrationEvent.kt
git commit -m "feat(book): add JSON integration event model"
```

### Task 3: Update OutboxRelayService

**Files:**
- Modify: `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/outbox/OutboxRelayService.kt`

- [ ] **Step 1: Update imports and implementation**

Inject `ObjectMapper` and use `BookIntegrationEvent`.

```kotlin
package com.quietchatter.book.adaptor.out.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.quietchatter.book.adaptor.out.messaging.BookIntegrationEvent
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxRelayService(
    private val outboxEventRepository: OutboxEventRepository,
    private val streamBridge: StreamBridge,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OutboxRelayService::class.java)

    @Scheduled(fixedDelayString = "\${outbox.relay.fixed-delay:1000}")
    @Transactional
    fun relayEvents() {
        val events = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))
        for (event in events) {
            val payloadMap: Map<String, Any?> = objectMapper.readValue(event.payload)
            
            val integrationEvent = BookIntegrationEvent(
                evtId = event.id.toString(),
                evtAggId = event.aggregateId,
                evtType = event.type,
                evtTime = event.createdAt.toString(),
                payload = payloadMap
            )

            val message = MessageBuilder.withPayload(integrationEvent)
                .setHeader(KafkaHeaders.KEY, event.aggregateId.toByteArray())
                .build()

            val success = streamBridge.send("bookEvents-out-0", message)
            if (success) {
                event.markProcessed()
                log.debug("Successfully relayed outbox event: ${event.id}")
            } else {
                log.error("Failed to relay outbox event: ${event.id}")
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out/outbox/OutboxRelayService.kt
git commit -m "feat(book): migrate OutboxRelayService to JSON"
```

### Task 4: Update Configuration

**Files:**
- Modify: `microservice-book/src/main/resources/application.yml`

- [ ] **Step 1: Remove Schema Registry and update content-type**

Remove:
- `spring.cloud.stream.kafka.binder.configuration.schema.registry.url`
- `content-type: application/*+avro` (change to `application/json`)

- [ ] **Step 2: Commit**

```bash
git add microservice-book/src/main/resources/application.yml
git commit -m "config(book): update stream configuration for JSON"
```

### Task 5: Cleanup and Verification

**Files:**
- Delete: `microservice-book/src/main/avro/BookEvent.avsc`

- [ ] **Step 1: Delete Avro schema file**

```bash
rm microservice-book/src/main/avro/BookEvent.avsc
```

- [ ] **Step 2: Verify compilation and tests**

Run: `./gradlew :microservice-book:build`

- [ ] **Step 3: Commit**

```bash
git add microservice-book/src/main/avro/BookEvent.avsc
git commit -m "chore(book): remove avro schema file"
```
