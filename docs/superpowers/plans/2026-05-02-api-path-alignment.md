# 프론트엔드 및 백엔드 API 경로 정렬 실행 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 프론트엔드의 API 호출 경로를 백엔드 마이크로서비스 명세에 맞게 전면 수정하고, 백엔드의 수정 메서드를 `PUT`으로 정렬합니다.

**Architecture:** 
1. 백엔드 `TalkController`의 `@PatchMapping`을 `@PutMapping`으로 변경하여 사용자의 `PUT` 선호도를 반영합니다.
2. 프론트엔드 `api.ts`의 모든 엔드포인트를 `/api` 기반의 새로운 구조로 업데이트합니다.
3. 필드명(`dateToHidden`) 및 파라미터 구조 불일치를 해결합니다.

**Tech Stack:** React (TypeScript), Kotlin (Spring Boot), Git

---

### Task 1: 백엔드 수정 (Talk Service)

**Files:**
- Modify: `microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/TalkController.kt`

- [ ] **Step 1: `@PatchMapping`을 `@PutMapping`으로 변경**

```kotlin
    @PutMapping("/{talkId}") // @PatchMapping에서 변경
    fun updateTalk(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @PathVariable talkId: UUID,
        @RequestBody request: UpdateTalkRequest
    ) {
        // ... 생략
    }
```

- [ ] **Step 2: 빌드 확인**

Run: `./gradlew :microservice-talk:build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add microservice-talk/src/main/kotlin/com/quietchatter/talk/adaptor/in/web/TalkController.kt
git commit -m "refactor(talk): change update talk mapping from PATCH to PUT"
```

---

### Task 2: 프론트엔드 API 클라이언트 및 엔드포인트 수정

**Files:**
- Modify: `frontend/src/api/api.ts`

- [ ] **Step 1: `api.ts` 파일의 모든 경로 및 메서드 업데이트**

주요 변경점:
- `/v1` -> `/api`
- `signup/naver` -> `signup`
- `me/profile` -> `members/me/profile`
- `me` (탈퇴) -> `members/me`
- `getTalks` 경로 변경 (`/api/talks/book/${bookId}`)
- `getMyTalks` 경로 변경 (`/api/talks?memberId=${id}`)
- `postTalk` 필드명 변경 (`hidden` -> `dateToHidden`)
- `handleReaction` 경로 변경 (`/api/reactions/talks/${talkId}`)
- `sendVocMessage` 경로 변경 (`/api/support/messages`)

수정된 코드 일부 예시:
```typescript
export async function updateTalk(talkId: string, content: string): Promise<void> {
  await apiClient.put(`/api/talks/${talkId}`, { content }); // v1 제거
}

export async function postTalk(bookId: string, content: string): Promise<Talk> {
  // ... 생략
  const response = await apiClient.post<Talk>('/api/talks', {
    bookId,
    content,
    dateToHidden: hiddenTimestamp // 필드명 변경
  });
  return response.data;
}
```

- [ ] **Step 2: 타입 정의 업데이트 확인**

**Files:**
- Modify: `frontend/src/types/index.ts` (필요 시)

`dateToHidden` 필드가 타입 정의에 반영되어 있는지 확인하고 수정합니다.

- [ ] **Step 3: 커밋**

```bash
git add frontend/src/api/api.ts frontend/src/types/
git commit -m "feat(frontend): align API paths and methods with backend specifications"
```

---

### Task 3: 연관된 React Hook 및 컴포넌트 수정

**Files:**
- Modify: `frontend/src/hooks/useMyTalks.ts`
- Modify: `frontend/src/hooks/useBookDetail.ts`
- Modify: `frontend/src/components/book/TalkList.tsx`

- [ ] **Step 1: `useMyTalks`에서 `memberId` 전달하도록 수정**

백엔드 명세에 따라 내 톡 조회 시 `memberId` 쿼리 파라미터가 필요합니다. `AuthContext` 등에서 ID를 가져와 전달하도록 수정합니다.

- [ ] **Step 2: `handleReaction` 호출부 수정**

경로 구조가 변경됨에 따라 파라미터 전달 방식이 올바른지 확인합니다.

- [ ] **Step 3: 커밋**

```bash
git add frontend/src/hooks/ frontend/src/components/
git commit -m "refactor(frontend): update hooks and components to match new API signatures"
```

---

### Task 4: 최종 검증 및 테스트

- [ ] **Step 1: 프론트엔드 타입 체크 및 빌드**

Run: `cd frontend && npm run type-check` (또는 해당 프로젝트의 빌드 명령)
Expected: No errors

- [ ] **Step 2: MSW 핸들러 업데이트 (사용 중인 경우)**

**Files:**
- Modify: `frontend/src/mocks/handlers.ts`

모든 모킹 경로를 `/api/**`로 업데이트합니다.

- [ ] **Step 3: 최종 커밋**

```bash
git add .
git commit -m "test: update msw handlers and finalize API alignment"
```
