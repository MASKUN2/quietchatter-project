# Standardize Talk API Paths Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize API paths in `microservice-talk` to use the `/api/{resource}` pattern.

**Architecture:** Update `@RequestMapping` and `@GetMapping` annotations in controllers and update corresponding test cases. Move path variables from class level to method level where necessary.

**Tech Stack:** Kotlin, Spring Boot, Spring Web, MockMvc, RestDocs

---

### Task 1: Update TalkController paths

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/TalkController.kt`

- [ ] **Step 1: Update class level @RequestMapping**
Change `@RequestMapping("/api/v1/talks")` to `@RequestMapping("/api/talks")`.

- [ ] **Step 2: Verify method level mappings**
Method level mappings like `@PostMapping`, `@PatchMapping("/{talkId}")`, etc., are relative to the class level mapping, so they don't need changes if they remain under `/api/talks`.

### Task 2: Update ReactionController paths

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/ReactionController.kt`

- [ ] **Step 1: Update class level @RequestMapping and move talkId to methods**
Change `@RequestMapping("/v1/talks/{talkId}/reactions")` to `@RequestMapping("/api/reactions")`.
Update `@PostMapping` to `@PostMapping("/talks/{talkId}")`.
Update `@DeleteMapping` to `@DeleteMapping("/talks/{talkId}")`.

### Task 3: Update SpecController path

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/SpecController.kt`

- [ ] **Step 1: Update @GetMapping path**
Change `@GetMapping("/api/v1/spec")` to `@GetMapping("/api/spec")`.

### Task 4: Update TalkControllerDocTest

**Files:**
- Modify: `microservice-talk/src/test/kotlin/com/quietchatter/talk/adaptor/in/web/TalkControllerDocTest.kt`

- [ ] **Step 1: Update MockMvc request paths**
Change all occurrences of `/api/v1/talks` to `/api/talks`.

### Task 5: Verify build and commit

- [ ] **Step 1: Run build**
Run: `./gradlew :microservice-talk:build -x test` in the root directory.
Expected: SUCCESS

- [ ] **Step 2: Commit changes**
Run: `git add microservice-talk && git commit -m "feat(talk): standardize api paths to /api/talks and /api/reactions"`
