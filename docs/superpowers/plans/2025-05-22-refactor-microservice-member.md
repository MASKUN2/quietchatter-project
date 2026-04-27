# microservice-member Refactoring Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor package names in `microservice-member` and improve null safety by replacing `!!` with safe handling.

**Architecture:** Rename `in`/`out` packages to `port_in`/`port_out` to avoid reserved words and improve idiomatic Kotlin null safety using `requireNotNull` or similar.

**Tech Stack:** Kotlin, Spring Boot, Gradle, Avro

---

### Task 1: Rename Packages and Directories

**Files:**
- Rename Directories:
  - `microservice-member/src/main/kotlin/com/quietchatter/member/application/in` -> `port_in`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/application/out` -> `port_out`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in` -> `port_in`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/out` -> `port_out`
- Modify: All `.kt` files in `microservice-member` (Update package and imports)

- [ ] **Step 1: Create new directories and move files**

Run commands to move files to new directory structure.

```bash
mv microservice-member/src/main/kotlin/com/quietchatter/member/application/in microservice-member/src/main/kotlin/com/quietchatter/member/application/port_in
mv microservice-member/src/main/kotlin/com/quietchatter/member/application/out microservice-member/src/main/kotlin/com/quietchatter/member/application/port_out
mv microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/port_in
mv microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/out microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/port_out
```

- [ ] **Step 2: Update package declarations and imports**

Update all `.kt` files:
- Replace `package com.quietchatter.member.application.in` with `package com.quietchatter.member.application.port_in`
- Replace `package com.quietchatter.member.application.out` with `package com.quietchatter.member.application.port_out`
- Replace `package com.quietchatter.member.adaptor.in` with `package com.quietchatter.member.adaptor.port_in`
- Replace `package com.quietchatter.member.adaptor.out` with `package com.quietchatter.member.adaptor.port_out`
- Replace `import com.quietchatter.member.application.in.` with `import com.quietchatter.member.application.port_in.`
- Replace `import com.quietchatter.member.application.out.` with `import com.quietchatter.member.application.port_out.`
- Replace `import com.quietchatter.member.adaptor.in.` with `import com.quietchatter.member.adaptor.port_in.`
- Replace `import com.quietchatter.member.adaptor.out.` with `import com.quietchatter.member.adaptor.port_out.`

- [ ] **Step 3: Update Avro Namespace**

Modify `microservice-member/src/main/avro/MemberEvent.avsc`:
Change namespace from `com.quietchatter.member.adaptor.out.messaging.avro` to `com.quietchatter.member.adaptor.port_out.messaging.avro`.

- [ ] **Step 4: Commit package changes**

```bash
git add microservice-member/src/main/kotlin microservice-member/src/main/avro
git commit -m "refactor: rename in/out packages to port_in/port_out in microservice-member"
```

### Task 2: Improve Null Safety

**Files:**
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/member/application/MemberService.kt`
- Modify: `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/port_in/web/AuthController.kt`

- [ ] **Step 1: Replace `!!` in MemberService.kt**

```kotlin
// In MemberService.kt
// replace tokenResponse.accessToken!! with requireNotNull(tokenResponse.accessToken) { "Access token missing" }
// replace profileResponse.response!!.id with requireNotNull(profileResponse.response).id
// replace member.id!! with requireNotNull(member.id) { "Member ID missing" }
```

- [ ] **Step 2: Replace `!!` in AuthController.kt**

```kotlin
// In AuthController.kt
// replace member.id!! with requireNotNull(member.id) { "Member ID missing" }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew clean compileKotlin` in `microservice-member` directory.

- [ ] **Step 4: Commit null safety changes**

```bash
git add microservice-member/src/main/kotlin
git commit -m "refactor: improve null safety by replacing !! with requireNotNull in microservice-member"
```
