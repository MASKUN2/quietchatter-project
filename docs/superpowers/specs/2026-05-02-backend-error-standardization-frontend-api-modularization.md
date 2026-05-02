# 설계 문서: 백엔드 에러 표준화 및 프론트엔드 API 모듈화

이 문서는 시스템의 일관성을 높이기 위해 백엔드의 에러 응답 형식을 RFC 7807로 표준화하고, 프론트엔드의 API 호출 레이어를 모듈화하기 위한 설계를 기술합니다.

## 1. 백엔드 에러 표준화 (RFC 7807)

모든 마이크로서비스가 동일한 에러 응답 구조를 갖도록 `microservice-book`의 방식을 확산합니다.

### A. 공통 에러 응답 형식
Spring 6의 `org.springframework.http.ProblemDetail`을 사용하여 다음과 같은 JSON 구조를 반환합니다.
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "에러 상세 메시지",
  "instance": "/api/..."
}
```

### B. 서비스별 구현
*   **microservice-member**: `com.quietchatter.member.adaptor.in.web.error.GlobalExceptionHandler` 생성
*   **microservice-talk**: `com.quietchatter.talk.adaptor.in.web.error.GlobalExceptionHandler` 생성
*   **공통 처리 예외**:
    *   `IllegalArgumentException` -> 400 Bad Request
    *   `NoSuchElementException` -> 404 Not Found
    *   `Exception` (Unhandled) -> 500 Internal Server Error

## 2. 프론트엔드 API 모듈화

단일 파일인 `api.ts`를 도메인별로 분리하여 코드 응집도를 높이고 유지보수를 용이하게 합니다.

### A. 파일 구조 설계 (`frontend/src/api/`)
1.  **`client.ts`**: Axios 인스턴스 설정, 인터셉터, `ApiError` 클래스
2.  **`auth.ts`**: 인증 및 회원 관련 (getMe, login, signup, logout, deactivate 등)
3.  **`books.ts`**: 도서 관련 (searchBooks, getBookDetails 등)
4.  **`talks.ts`**: 톡 및 반응 관련 (getTalks, postTalk, updateTalk, deleteTalk, handleReaction 등)
5.  **`support.ts`**: 고객 지원 관련 (sendVocMessage)

### B. 영향도 및 작업 내용
*   기존 `api.ts`는 삭제하고 기능을 위 파일들로 분산
*   프론트엔드 내의 모든 API 참조(Hooks, Context, Pages) 경로 업데이트
    *   `import { ... } from '../api/api'` -> `import { ... } from '../api/auth'` 등

## 3. 구현 단계
1.  **백엔드**: `member` 서비스와 `talk` 서비스에 예외 핸들러 구현 및 빌드 검증
2.  **프론트엔드**: `api/` 디렉토리 하위 파일 생성 및 코드 이전
3.  **참조 업데이트**: 프론트엔드 전체 소스 코드를 대상으로 API import 경로 수정
4.  **검증**: 백엔드 빌드 테스트 및 프론트엔드 타입 체크

## 4. 검증 계획
*   백엔드: `./gradlew build` 성공 확인
*   프론트엔드: `npx tsc --noEmit`을 통한 타입 안전성 검증
