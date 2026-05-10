# Agent Working Procedure

1. 에이전트는 `## 요구사항` 을 구체화하기 위해 필요한 질문들을 사용자와 나눕니다. 질문은 에이전트가 제안하는 선택지가 포함되어 있습니다.
2. 에이전트는 답변을 통해 `## 요구사항`을 구체적으로 수정합니다. 요구사항에는 정제된 요구사항만이 담기며 발생한 의사결정은 `## 의사결정 히스토리`에 간략하게 더하는 방식으로 기록합니다.
3. 에이전트는 사용자에게 `## 요구사항` 리뷰를 요청합니다.
4. 사용자가 `## 요구사항` 리뷰를 승인하면 에이전트는 `## 작업계획`을 작성합니다.
5. 에이전트는 `## 작업계획` 리뷰를 사용자에게 요청합니다.
6. 사용자가 `## 작업계획` 리뷰를 승인하면 에이전트는 작업을 진행합니다.
7. 만약 작업중 `## 요구사항` 과 `## 의사결정 히스토리`에 대해 재검토가 필요하면 즉시 작업을 중단하고 **1번**으로 돌아갑니다.
8. 에이전트가 모든 작업을 마쳤으면 `## 작업결과`를 작성합니다.
9. 각 작성물은 KST 기준으로 시간을 기록합니다.

## 요구사항

사용자가 마이페이지에서 자신의 숨겨진 북톡을 조회하고, 숨김을 해제할 수 있어야 한다.

### 상세 요구사항

1. **마이페이지 탭 UI**: "공개" / "숨겨진" 탭으로 구분하여 톡 목록 전환
2. **숨겨진 톡 조회 API**: `GET /api/talks?memberId=...&hidden=true` (`hidden` 파라미터 기본값 `false`)
   - `hidden=false` 또는 파라미터 생략: 기존처럼 누구나 공개 톡 조회 가능
   - `hidden=true`: `X-Member-Id != memberId`이면 403 반환 (본인만 조회 가능)
3. **숨김 해제**: 숨겨진 톡에 "숨김 해제" 버튼 → 클릭 시 `isHidden = false`, `dateToHidden = 오늘 + 12개월`로 초기화
4. **숨김 해제 API**: `POST /api/talks/{talkId}/restore`

## 의사결정 히스토리

- **UI 구조**: 별도 섹션/토글 대신 탭 전환 방식 선택 — 공개/숨겨진 목록이 명확히 분리됨
- **허용 액션**: 단순 조회가 아닌 숨김 해제 기능 포함 — 해제 시 1년 공개 기간 재설정
- **API 방식**: 별도 엔드포인트 대신 기존 `GET /api/talks`에 `hidden=true` 쿼리 파라미터 추가 — 일관성 유지
- **소유자 검증 위치**: 컨트롤러 분기 대신 서비스 레이어에서 `ForbiddenException` throw — 비즈니스 규칙은 서비스가 책임, 컨트롤러는 라우팅만 담당
- **커스텀 예외**: `ForbiddenException` 도입, `GlobalExceptionHandler`에서 403으로 변환 — `IllegalArgumentException`(400)과 명확히 구분
- **쿼리 메서드 분리**: `getTalksByMember(hidden, requesterId?)` 하나에서 `getVisibleTalksByMember` / `getHiddenTalksByMember`로 분리 — nullable 파라미터 제거, 두 동작이 명확히 구분됨
- **중복 응답 모델 제거**: `TalkResponse` + `toResponse()` 제거, `TalkDetail`을 직접 반환 — 동일한 구조를 두 개로 유지할 이유 없음
- **도메인 서비스 도입**: `TalkOwnershipService` 신규 도입 — hide/restore/updateContent 모두 행위자(Owner/Admin/System)별로 처리, `verifyOwner` private 메서드 제거, `ForbiddenException`을 `domain` 패키지로 이동
- **통합 테스트**: `@EmbeddedKafka` + `@MockitoBean(MemberClient)` 조합으로 외부 의존 없이 전 시나리오 검증

## 작업계획

> 작성 시각: 2026-05-10 KST

### Backend (microservice-talk)

1. **`Talk.kt`** — `restore()` 메서드 추가: `isHidden = false`, `dateToHidden = 오늘 + 12개월`
2. **`TalkJpaRepository.kt`** — `findByMemberIdAndIsHidden(memberId, isHidden, pageable)` 쿼리 추가
3. **`TalkLoadable.kt`** — 포트에 `findByMemberIdAndIsHidden(memberId, isHidden, pageable)` 추가
4. **`TalkPersistenceAdapter.kt`** — 위 포트 구현
5. **`TalkCommandable.kt`** — `restoreTalk(command: RestoreTalkCommand)` + `RestoreTalkCommand` 추가
6. **`TalkQueryable.kt`** — `getTalksByMember` 시그니처에 `hidden: Boolean` 파라미터 추가
7. **`TalkService.kt`** — `restoreTalk` 구현, `getTalksByMember` 분기 처리
8. **`TalkController.kt`**
   - `GET /api/talks`: `@RequestParam hidden: Boolean = false` 추가, 호출부 수정
   - `POST /api/talks/{talkId}/restore` 엔드포인트 추가

### Frontend

9. **`api/talks.ts`** — `getMyTalks`에 `hidden` 파라미터 추가, `restoreTalk(talkId)` API 함수 추가
10. **`hooks/useMyTalks.ts`** — `hidden` 플래그 파라미터 수용, 탭 전환 시 목록 초기화 후 재조회
11. **`components/book/TalkItem.tsx`** — `isHiddenMode` prop 추가 → hidden 모드일 때 수정/삭제 대신 "숨김 해제" 버튼 표시
12. **`pages/MyPage/MyPage.tsx`** — MUI `Tabs`로 "공개" / "숨겨진" 탭 추가, 탭 상태를 `useMyTalks`에 전달

## 작업결과

> 완료 시각: 2026-05-10 KST

### 변경 파일 목록

**Backend**
- `domain/Talk.kt` — `restore()` 메서드 추가
- `domain/ForbiddenException.kt` (신규) — 커스텀 예외 클래스 (`exception/` 패키지에서 이동)
- `domain/TalkOwnershipService.kt` (신규) — 행위자별 북톡 상태 변경 도메인 서비스 (hideByOwner/Admin/System, restoreByOwner, updateContentByOwner, authorizeHiddenAccess)
- `adaptor/out/persistence/TalkJpaRepository.kt` — `findByMemberIdAndIsHidden` 쿼리 추가
- `application/out/TalkLoadable.kt` — 포트 메서드 추가
- `adaptor/out/persistence/TalkPersistenceAdapter.kt` — 포트 구현
- `application/in/TalkCommandable.kt` — `restoreTalk`, `RestoreTalkCommand` 추가
- `application/in/TalkQueryable.kt` — `getVisibleTalksByMember` / `getHiddenTalksByMember`로 분리 (nullable 제거)
- `application/TalkService.kt` — `restoreTalk` 구현, `TalkOwnershipService` 주입, `verifyOwner` 제거, `TalkResponse` 제거 및 `TalkDetail` 직접 반환
- `adaptor/in/web/TalkController.kt` — `hidden=false` 기본값 파라미터, `POST /{talkId}/restore` 추가, memberId 필수 파라미터화
- `adaptor/in/web/error/GlobalExceptionHandler.kt` — `ForbiddenException` → 403 핸들러 추가
- `test/.../HiddenTalkIntegrationTest.kt` (신규) — `@EmbeddedKafka` + `@MockitoBean(MemberClient)` 통합 테스트 (7개 시나리오)
- `test/.../TalkControllerDocTest.kt` — 시그니처 업데이트, `restoreTalk` 테스트 추가, Forbidden 테스트에 예외 stub 적용
- `test/.../GlobalExceptionHandlerTest.kt` — `ForbiddenException` 403 변환 검증 테스트 추가
- `test/.../ServiceTests.kt` — `TalkOwnershipService` 생성자 인자 추가
- `build.gradle.kts` — `spring-kafka-test` 의존성 추가

**Frontend**
- `api/talks.ts` — `getMyTalks` hidden 파라미터 추가, `restoreTalk` 추가
- `hooks/useMyTalks.ts` — `hidden` 플래그 수용, 탭 전환 시 재조회, `handleRestoreTalk` 추가
- `components/book/TalkItem.tsx` — `isHiddenMode`, `onRestore` prop 추가, 숨김 해제 버튼
- `pages/MyPage/MyPage.tsx` — "공개" / "숨겨진" 탭 추가