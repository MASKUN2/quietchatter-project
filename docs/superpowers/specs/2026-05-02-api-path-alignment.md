# 설계 문서: 프론트엔드 API 경로 및 명세 정렬

이 문서는 프론트엔드(`frontend`)의 API 호출부를 현재 마이크로서비스 백엔드(`book`, `member`, `talk`)의 실제 구현 명세에 맞게 수정하기 위한 설계를 기술합니다.

## 1. 주요 변경 원칙
*   **베이스 경로 통일**: `/v1` 접두어를 제거하고 백엔드 표준인 `/api`를 사용합니다.
*   **리소스 중심 경로 재설계**: 백엔드의 RESTful 경로 구조(예: `/api/members/me`, `/api/talks/book/{id}`)를 따릅니다.
*   **HTTP 메서드 정렬**: 백엔드 컨트롤러에 정의된 메서드(예: `PATCH` 등)를 정확히 사용합니다.

## 2. API별 변경 명세

### A. 인증 및 회원 (Member Service)
| 기능 | 기존 경로 (Old) | 변경 경로 (New) | 비고 |
| :--- | :--- | :--- | :--- |
| 내 정보 조회 | `GET /v1/auth/me` | `GET /api/auth/me` | |
| 네이버 로그인 | `POST /v1/auth/login/naver` | `POST /api/auth/login/naver` | |
| 네이버 가입 | `POST /v1/auth/signup/naver` | `POST /api/auth/signup` | 경로 간소화 |
| 프로필 수정 | `PUT /v1/me/profile` | `PUT /api/members/me/profile` | |
| 회원 탈퇴 | `DELETE /v1/me` | `DELETE /api/members/me` | |

### B. 도서 (Book Service)
| 기능 | 기존 경로 (Old) | 변경 경로 (New) | 비고 |
| :--- | :--- | :--- | :--- |
| 도서 검색 | `GET /v1/books` | `GET /api/books` | keyword 파라미터 유지 |
| 도서 상세 | `GET /v1/books/{id}` | `GET /api/books/{id}` | |

### C. 톡 및 반응 (Talk Service)
| 기능 | 기존 경로 (Old) | 변경 경로 (New) | 비고 |
| :--- | :--- | :--- | :--- |
| 도서별 톡 조회 | `GET /v1/talks` | `GET /api/talks/book/{bookId}` | **구조 변경** |
| 추천 톡 조회 | `GET /v1/talks/recommend` | `GET /api/talks/recommended` | 경로명 수정 |
| 내 톡 조회 | `GET /v1/me/talks` | `GET /api/talks?memberId={id}` | **구조 변경** |
| 톡 등록 | `POST /v1/talks` | `POST /api/talks` | `dateToHidden` 필드명 확인 |
| 톡 수정 | `PUT /v1/talks/{id}` | **`PUT`** `/api/talks/{id}` | **PUT 유지** (백엔드 수정 필요) |
| 반응 처리 | `POST /v1/reactions` | `POST /api/reactions/talks/{id}` | **구조 변경** |

### D. 고객 센터 (Support Service)
| 기능 | 기존 경로 (Old) | 변경 경로 (New) | 비고 |
| :--- | :--- | :--- | :--- |
| 메시지 전송 | `POST /v1/customer/messages` | `POST /api/support/messages` | |

## 3. 필드명 정렬 (Field Alignment)
*   **톡 등록/수정**: 프론트엔드의 `hidden` 필드명을 백엔드 DTO 명세인 `dateToHidden`으로 변경합니다.
*   **응답 처리**: 백엔드 응답의 `UUID` 및 `LocalDateTime` 형식이 프론트엔드 타입과 호환되는지 확인합니다.

## 4. 구현 단계
1.  **백엔드 수정**: `TalkController`의 `@PatchMapping`을 `@PutMapping`으로 변경
2.  **프론트엔드 수정**: `frontend/src/api/api.ts` 내의 모든 함수 경로 및 메서드 일괄 수정
3.  요청/응답 인터페이스(`types/index.ts` 등)와 백엔드 DTO 간의 필드 불일치 확인 및 수정
4.  API 호출을 사용하는 React Hook 및 컴포넌트의 파라미터 전달 방식 조정

## 4. 검증 계획
*   빌드 오류 확인 (TypeScript 타입 체크)
*   모의 서버(MSW) 환경의 핸들러 경로도 함께 업데이트하여 테스트 환경 일관성 유지
