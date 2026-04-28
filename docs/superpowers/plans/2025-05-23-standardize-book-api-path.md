# Microservice-Book Controller Path Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize API paths in `microservice-book` to use the `/api/books` pattern instead of `/v1/books`.

**Architecture:** Update the `@RequestMapping` in the controller, adjust REST client calls in tests, and update documentation to reflect the new path.

**Tech Stack:** Kotlin, Spring Boot, JUnit, Gradle.

---

### Task 1: Update Controller and Tests

**Files:**
- Modify: `microservice-book/src/main/kotlin/com/quietchatter/book/adaptor/in/web/BookApi.kt`
- Modify: `microservice-book/src/test/kotlin/com/quietchatter/book/adaptor/in/web/BookApiTest.kt`

- [ ] **Step 1: Update BookApi.kt**
    Change `@RequestMapping("/v1/books")` to `@RequestMapping("/api/books")`.

- [ ] **Step 2: Update BookApiTest.kt**
    Update all occurrences of `/v1/books` to `/api/books`.

---

### Task 2: Update Documentation

**Files:**
- Modify: `microservice-book/CLAUDE.md`
- Modify: `microservice-book/GEMINI.md`
- Modify: `microservice-book/docs/spec.md`

- [ ] **Step 1: Update CLAUDE.md**
    Update the API path description from `/v1/books` to `/api/books`.

- [ ] **Step 2: Update GEMINI.md**
    Update the API path description from `/v1/books` to `/api/books`.

- [ ] **Step 3: Update docs/spec.md**
    Update all API endpoint paths from `/v1/books` to `/api/books`.

---

### Task 3: Verification and Commit

- [ ] **Step 1: Run build**
    Run: `./gradlew :microservice-book:build -x test` in the root directory.
    Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run tests (optional but recommended)**
    Run: `./gradlew :microservice-book:test`
    Expected: All tests pass.

- [ ] **Step 3: Commit changes**
    Run: `git add microservice-book && git commit -m "feat(book): standardize api path to /api/books"`
