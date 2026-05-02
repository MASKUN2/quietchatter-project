# 서브모듈 이름 및 경로 변경 (frontend) 실행 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `legacy-quiet-chatter-front-end` 서브모듈을 `frontend`로 변경하고 새로운 GitHub URL을 반영합니다.

**Architecture:** `git mv`를 사용하여 물리적 경로를 이동하고, `.gitmodules` 설정을 수동으로 업데이트한 후 `git submodule sync`로 설정을 동기화합니다.

**Tech Stack:** Git, Bash

---

### Task 1: 물리적 경로 이동 및 인덱스 업데이트

**Files:**
- Modify: `.gitmodules` (자동 수정 포함)
- Rename: `legacy-quiet-chatter-front-end/` -> `frontend/`

- [ ] **Step 1: 폴더 이동 및 Git 인덱스 업데이트**

Run: `git mv legacy-quiet-chatter-front-end frontend`
Expected: 폴더 이름이 변경되고 `git status`에서 rename으로 표시됨

- [ ] **Step 2: 커밋 (중간 단계)**

```bash
git add frontend .gitmodules
git commit -m "refactor: rename submodule directory to frontend"
```

---

### Task 2: .gitmodules 설정 및 URL 업데이트

**Files:**
- Modify: `.gitmodules`

- [ ] **Step 1: .gitmodules 파일의 섹션 이름 및 URL 수정**

`.gitmodules` 파일에서 다음 내용을 수정합니다:
1. `[submodule "legacy-quiet-chatter-front-end"]` -> `[submodule "frontend"]`
2. `url = https://github.com/MASKUN2/quietchatter-frontend`

수정 후 내용 예시:
```ini
[submodule "frontend"]
	path = frontend
	url = https://github.com/MASKUN2/quietchatter-frontend
```

- [ ] **Step 2: 변경 사항 저장 및 커밋**

```bash
git add .gitmodules
git commit -m "config: update submodule name and url in .gitmodules"
```

---

### Task 3: Git 설정 동기화 및 검증

- [ ] **Step 1: 서브모듈 설정 동기화**

Run: `git submodule sync`
Expected: `Synchronizing submodule url for 'frontend'` 메시지 출력

- [ ] **Step 2: 서브모듈 초기화 및 업데이트 확인**

Run: `git submodule update --init --recursive`
Expected: 새로운 URL로부터 정상적으로 데이터를 가져오거나 이미 최신 상태임을 확인

- [ ] **Step 3: 최종 상태 확인**

Run: `git status`
Expected: 깨끗한 상태 (모든 변경사항 커밋됨)

- [ ] **Step 4: 최종 커밋 (필요 시)**

```bash
git add .
git commit -m "feat: complete submodule migration to frontend"
```
