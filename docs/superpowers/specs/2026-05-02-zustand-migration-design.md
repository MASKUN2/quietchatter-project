# 설계 문서: Zustand를 활용한 전역 상태 관리 고도화

이 문서는 프론트엔드(`frontend`)의 전역 상태 관리 도구를 Context API에서 Zustand로 전환하여 코드 복잡도를 낮추고 성능을 최적화하기 위한 설계를 기술합니다.

## 1. 도입 배경 및 목적
*   **Context API의 한계**: 상태 변경 시 구독 중인 모든 컴포넌트 리렌더링 발생
*   **미들웨어 활용**: `persist` 미들웨어를 사용하여 로컬 스토리지 동기화 자동화
*   **접근성 향상**: Hook 규칙에 얽매이지 않고 일반 로직(Axios interceptor 등)에서도 상태 접근 가능

## 2. Store 설계

### A. Toast Store (`frontend/src/store/useToastStore.ts`)
*   **상태(State)**: `open`, `message`, `severity` (info, success, warning, error)
*   **액션(Actions)**:
    *   `showToast(message, severity)`: 토스트 표시
    *   `hideToast()`: 토스트 닫기
*   **이점**: `ToastProvider` 렌더링 영역과 상태 관리 영역 분리

### B. Auth Store (`frontend/src/store/useAuthStore.ts`)
*   **상태(State)**: `member` (유저 정보 객체), `loading`
*   **액션(Actions)**:
    *   `setMember(member)`: 유저 정보 업데이트
    *   `clearMember()`: 로그아웃 처리
    *   `refreshMember()`: API 호출을 통한 최신 정보 갱신
*   **미들웨어**: `persist`를 사용하여 `member` 정보를 `auth_storage`라는 키로 로컬 스토리지에 자동 저장

## 3. 구현 단계
1.  **패키지 설치**: `zustand` 라이브러리 추가
2.  **Store 생성**: `useToastStore`, `useAuthStore` 구현
3.  **컴포넌트 연동**:
    *   `ToastProvider`를 Zustand 상태를 구독하는 컴포넌트로 리팩토링
    *   `AuthContext` 제거 및 `useAuth` 사용처를 `useAuthStore`로 교체
4.  **참조 업데이트**: 전체 프로젝트의 Context 참조를 Store 참조로 일괄 수정
5.  **검증**: 기존 기능(로그인 유지, 토스트 알림) 정상 동작 확인

## 4. 즉시 커밋 전략
각 단계(패키지 설치, 개별 Store 생성, 컴포넌트 연동 등)가 완료될 때마다 즉시 로컬 커밋을 수행합니다.
