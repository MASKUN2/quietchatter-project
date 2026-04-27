# Spec: Migration from Avro to Flattened JSON Messaging

This specification outlines the transition from Avro-based messaging with Redpanda Schema Registry to a simplified, flattened JSON messaging format across all microservices.

## 1. Background & Purpose

The current system uses Apache Avro for event serialization and Redpanda Schema Registry for schema management. While this provides strong type safety and schema evolution, it introduces complexity in build processes and local development. 

To simplify the infrastructure and development workflow, we are moving to a **Flattened JSON** format. This removes the dependency on Avro plugins, generated classes, and the Schema Registry.

## 2. Design Goals

- **Simplicity:** Remove Avro-related build steps and infrastructure.
- **Readability:** Human-readable messages in Kafka/Redpanda.
- **Conflict Avoidance:** Use clear prefixes for metadata to prevent collision with business data.
- **Independence:** Each microservice defines its own DTOs for the events it consumes.

## 3. Message Structure

### 3.1 Metadata Fields
All metadata fields will be prefixed with `evt_` to distinguish them from business payload fields.

| Field Name | Description | Example |
| :--- | :--- | :--- |
| `evt_id` | Unique event ID (UUID) | `550e8400-e29b-41d4-a716-446655440000` |
| `evt_agg_id` | Aggregate ID the event belongs to | `MEMBER_123` |
| `evt_type` | Specific event type name | `MemberDeactivatedEvent` |
| `evt_time` | ISO-8601 timestamp of when the event occurred | `2026-04-27T10:00:00Z` |

### 3.2 Payload Flattening
Previously, business data was nested within a `payload` field as a JSON string. In the new format, all keys from this payload will be flattened to the top level.

**Example Transformation:**

*Current (Avro):*
```json
{
  "eventId": "uuid-123",
  "aggregateId": "member-456",
  "type": "MemberDeactivatedEvent",
  "payload": "{\"memberId\":\"member-456\", \"reason\":\"quit\"}",
  "occurredAt": "2026-04-27T10:00:00Z"
}
```

*New (Flattened JSON):*
```json
{
  "evt_id": "uuid-123",
  "evt_agg_id": "member-456",
  "evt_type": "MemberDeactivatedEvent",
  "evt_time": "2026-04-27T10:00:00Z",
  "memberId": "member-456",
  "reason": "quit"
}
```

## 4. Architectural Changes

### 4.1 Producers (Outbox Relay)
- **DTOs:** Define a base `IntegrationEvent` class or interface in each service to handle the `evt_` fields.
- **Processing:** The `OutboxRelayService` will:
    1. Read the `OutboxEvent` from the DB.
    2. Parse the `payload` (JSON string) into a Map.
    3. Construct the flattened JSON object (Map or DTO) including the `evt_` metadata.
    4. Send via `StreamBridge` with `application/json` content type.

### 4.2 Consumers
- **Independence:** Each consumer service defines its own DTO matching the expected event structure.
- **Deserialization:** Spring Cloud Stream will automatically deserialize the JSON into the target DTO.
- **Logic:** Use `evt_type` to route or handle different event types.

### 4.3 Build & Dependencies
- Remove `com.github.davidmc24.gradle.plugin.avro` plugin.
- Remove `io.confluent:kafka-avro-serializer`.
- Remove `org.apache.avro:avro`.
- Delete all `src/main/avro/*.avsc` files.
- Remove `avro { ... }` configuration blocks from `build.gradle.kts`.

### 4.4 Configuration (`application.yml`)
- Change `spring.cloud.stream.bindings.<channel>.content-type` to `application/json`.
- Remove Schema Registry URLs and Avro-specific serializer/deserializer configurations.

## 5. Migration Strategy

The migration will be performed incrementally to ensure system stability:

1. **Member Service (Producer):** Update `OutboxRelayService` to emit JSON.
2. **Talk Service (Consumer):** Update `MemberEventConsumer` to handle JSON.
3. **Book Service (Producer/Consumer):** Repeat the process for Book-related events.
4. **Cleanup:** Once all services are migrated, remove all Avro-related files and configurations.

## 6. Testing & Validation
- **Unit Tests:** Verify the `OutboxRelayService` correctly flattens the payload.
- **Integration Tests:** Use `@SpringBootTest` with `TestContainers` (Kafka) to verify end-to-end JSON messaging between services.
- **Manual Verification:** Inspect messages in Redpanda Console to ensure the format matches the specification.
