# Refactor in/out Packages and Null Safety

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename reserved `in` and `out` packages to `port_in` and `port_out` and replace `!!` with safe null handling in `microservice-book`, `microservice-member`, and `microservice-talk`.

**Architecture:** Package renaming to avoid Kotlin reserved words and improving null safety by replacing `!!` with idiomatic alternatives.

**Tech Stack:** Kotlin, Spring Boot, Gradle.

---

### Task 1: Refactor `microservice-book`

**Files:**
- Modify: All `.kt` files in `microservice-book/src`
- Rename: `in` -> `port_in`, `out` -> `port_out` directories

- [ ] **Step 1: Rename directories in `src/main/kotlin`**
  - `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/in` -> `port_in`
  - `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/out` -> `port_out`
  - `microservice-book/src/main/kotlin/com/quietchatter/book/application/in` -> `port_in`
  - `microservice-book/src/main/kotlin/com/quietchatter/book/application/port/out` -> `port_out`

- [ ] **Step 2: Rename directories in `src/test/kotlin`**
  - `microservice-book/src/test/kotlin/com/quietchatter/book/adaptor/in` -> `port_in`
  - `microservice-book/src/test/kotlin/com/quietchatter/book/adaptor/out` -> `port_out`

- [ ] **Step 3: Update package declarations and imports in all `.kt` files**
  - Use `sed` or similar to replace `.in.` with `.port_in.` and `.out.` with `.port_out.` in package and import statements.
  - Also handle ```.`in`.``` and ```.`out`.``` if they exist.

- [ ] **Step 4: Fix `!!` usages**
  - `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/in/web/BookResponse.kt` (now `port_in`): replace `book.id!!` with `requireNotNull(book.id)` or similar.

- [ ] **Step 5: Verify build**
  - Run: `./gradlew :microservice-book:clean :microservice-book:compileKotlin`

- [ ] **Step 6: Commit**
  - `git add microservice-book`
  - `git commit -m "refactor(book): rename in/out packages and improve null safety"`

---

### Task 2: Refactor `microservice-member`

**Files:**
- Modify: All `.kt` files in `microservice-member/src`
- Rename: `in` -> `port_in`, `out` -> `port_out` directories

- [ ] **Step 1: Rename directories in `src/main/kotlin`**
  - `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in` -> `port_in`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/out` -> `port_out`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/application/in` -> `port_in`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/application/out` -> `port_out`

- [ ] **Step 2: Rename directories in `src/test/kotlin`**
  - `microservice-member/src/test/kotlin/com/quietchatter/member/adaptor/out` -> `port_out`

- [ ] **Step 3: Update package declarations and imports in all `.kt` files**
  - Replace `.in.` with `.port_in.` and `.out.` with `.port_out.` in package and import statements.

- [ ] **Step 4: Fix `!!` usages**
  - `microservice-member/src/main/kotlin/com/quietchatter/member/adaptor/in/web/AuthController.kt` (now `port_in`): `member.id!!`
  - `microservice-member/src/main/kotlin/com/quietchatter/member/application/MemberService.kt`: `tokenResponse.accessToken!!`, `profileResponse.response!!`, `member.id!!`

- [ ] **Step 5: Verify build**
  - Run: `./gradlew :microservice-member:clean :microservice-member:compileKotlin`

- [ ] **Step 6: Commit**
  - `git add microservice-member`
  - `git commit -m "refactor(member): rename in/out packages and improve null safety"`

---

### Task 3: Refactor `microservice-talk`

**Files:**
- Modify: All `.kt` files in `microservice-talk/src`
- Rename: `in` -> `port_in`, `out` -> `port_out` directories

- [ ] **Step 1: Rename directories in `src/main/kotlin`**
  - `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in` -> `port_in`
  - `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/out` -> `port_out`
  - `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/in` -> `port_in`
  - `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/out` -> `port_out`

- [ ] **Step 2: Rename directories in `src/test/kotlin`**
  - `microservice-talk/src/test/kotlin/com/quietchatter/talk/adaptor/in` -> `port_in`
  - `microservice-talk/src/test/kotlin/com/quietchatter/talk/adaptor/out` -> `port_out`

- [ ] **Step 3: Update package declarations and imports in all `.kt` files**
  - Replace `.in.` with `.port_in.` and `.out.` with `.port_out.` in package and import statements.

- [ ] **Step 4: Fix `!!` usages**
  - `microservice-talk/src/main/kotlin/com/quietchatter/talk/application/TalkService.kt`: many `talk.id!!`, `talk.createdAt!!`, `talkPersistable.save(talk).id!!`

- [ ] **Step 5: Verify build**
  - Run: `./gradlew :microservice-talk:clean :microservice-talk:compileKotlin`

- [ ] **Step 6: Commit**
  - `git add microservice-talk`
  - `git commit -m "refactor(talk): rename in/out packages and improve null safety"`

---

### Final Verification
- [ ] **Run all builds together**
  - Run: `./gradlew clean compileKotlin`
