# Zustand 마이그레이션 및 상태 관리 고도화 실행 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Context API 기반의 상태 관리(`Auth`, `Toast`)를 Zustand로 마이그레이션하여 성능을 최적화하고 코드를 간결화합니다.

**Architecture:** 
- `frontend/src/store/` 디렉토리에 도메인별 Store 정의
- `persist` 미들웨어를 사용하여 로컬 스토리지 자동 동기화 적용
- `AuthContext`, `ToastContext` 제거 및 전역 Provider 최소화

**Tech Stack:** React (TypeScript), Zustand, Git

---

### Task 1: 패키지 설치 및 초기 설정

- [ ] **Step 1: Zustand 패키지 설치**

Run: `cd frontend && npm install zustand`
Expected: `package.json`에 `zustand` 추가됨

- [ ] **Step 2: 커밋**

```bash
git add frontend/package.json frontend/package-lock.json
git commit -m "chore(frontend): install zustand"
```

---

### Task 2: Toast Store 구현 및 연동

**Files:**
- Create: `frontend/src/store/useToastStore.ts`
- Modify: `frontend/src/providers/ToastProvider.tsx`
- Delete: `frontend/src/context/ToastContext.tsx`

- [ ] **Step 1: useToastStore 생성**

```typescript
import { create } from 'zustand';
import type { Severity } from '../types/ToastTypes';

interface ToastState {
  open: boolean;
  message: string;
  severity: Severity;
  showToast: (message: string, severity?: Severity) => void;
  hideToast: () => void;
}

export const useToastStore = create<ToastState>((set) => ({
  open: false,
  message: '',
  severity: 'info',
  showToast: (message, severity = 'info') => set({ open: true, message, severity }),
  hideToast: () => set({ open: false }),
}));
```

- [ ] **Step 2: ToastProvider 리팩토링**

`ToastProvider`에서 기존 `useState`를 제거하고 `useToastStore`의 상태를 구독하도록 수정합니다. `ToastContext.Provider`를 제거합니다.

- [ ] **Step 3: 커밋**

```bash
git add frontend/src/store/useToastStore.ts frontend/src/providers/ToastProvider.tsx
git commit -m "feat(frontend): implement ToastStore and refactor ToastProvider"
```

---

### Task 3: Auth Store 구현 (Persistence 적용)

**Files:**
- Create: `frontend/src/store/useAuthStore.ts`
- Delete: `frontend/src/context/AuthContext.tsx`

- [ ] **Step 1: useAuthStore 생성**

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Member } from '../types';
import { getMe, logout as logoutApi } from '../api/auth';

interface AuthState {
  member: Member | null;
  loading: boolean;
  setMember: (member: Member | null) => void;
  refreshMember: () => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      member: null,
      loading: true,
      setMember: (member) => set({ member, loading: false }),
      refreshMember: async () => {
        try {
          const memberData = await getMe();
          set({ member: memberData, loading: false });
        } catch {
          set({ member: null, loading: false });
        }
      },
      logout: async () => {
        try {
          await logoutApi();
        } finally {
          set({ member: null });
        }
      },
    }),
    {
      name: 'auth-storage', // 로컬 스토리지 키
      partialize: (state) => ({ member: state.member }), // member만 저장
    }
  )
);
```

- [ ] **Step 2: 커밋**

```bash
git add frontend/src/store/useAuthStore.ts
git commit -m "feat(frontend): implement AuthStore with persistence"
```

---

### Task 4: 프로젝트 전체 참조 업데이트 및 정리

**Files:**
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/**/*.tsx`, `frontend/src/**/*.ts` (useAuth, useToast 사용처)

- [ ] **Step 1: App.tsx에서 Context Provider 제거**

`AuthProvider`, `ToastProvider` 중 불필요해진 것을 제거하거나 순수 UI 렌더링용으로만 남깁니다.

- [ ] **Step 2: useAuth, useToast 사용처를 Store로 교체**

(예: `const { member } = useAuth()` -> `const { member } = useAuthStore()`)

- [ ] **Step 3: 사용되지 않는 Context 파일 삭제**

`frontend/src/context/AuthContext.tsx`, `frontend/src/context/ToastContext.tsx` 삭제

- [ ] **Step 4: 최종 커밋**

```bash
git add .
git commit -m "refactor(frontend): replace Context API with Zustand stores across the app"
```
