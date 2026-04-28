---
name: doc-structure
description: Use when reviewing or updating markdown documentation in any submodule. Defines the standard three-file structure and content separation rules for all QuietChatter submodules.
---

# 문서 구조 표준

## 파일 구성 규칙

서브모듈 루트에는 마크다운 파일이 3개만 존재한다.

- README.md: 서비스를 이해하기 위한 모든 내용 (기술 스택, 구조, API 규칙, 실행 방법 등)
- CLAUDE.md: AI 작업 지침 전용. README.md 읽기 안내로 시작. README.md와 내용 중복 금지.
- GEMINI.md: `MUST READ CLAUDE.md.` 한 줄만 작성.

docs/ 폴더는 비워두는 것을 원칙으로 한다. spec.md, tasks/ 등 기존 파일은 내용을 README.md에 흡수하고 삭제한다.

## CLAUDE.md 구조

```markdown
# CLAUDE.md - {서비스명}

작업 전 README.md를 읽으십시오. {서비스 개요, 기술 스택, 구조 등}은 README.md에 있습니다.

루트 프로젝트의 CLAUDE.md에 정의된 공통 원칙도 확인하십시오.

## 작업 지침

### A. ...
```

CLAUDE.md에 포함하는 내용: 작업 시 주의해야 할 규칙, 패턴, 금지 사항, 테스트 기준.
CLAUDE.md에 포함하지 않는 내용: 기술 스택, 아키텍처 설명, 라우팅 테이블, 실행 방법 (→ README.md).

## README.md 작성 규칙

- 굵게(bold), 기울임(italic) 서식 금지. 평문(Plain Text)만 사용.
- 이모티콘 사용 금지.
- 내용: 서비스 역할, 기술 스택, 패키지 구조, 주요 API 또는 도메인, 실행 방법.

## 절차

1. 현재 docs/ 아래 파일 내용을 README.md에 통합한다.
2. README.md의 기존 bold/italic 서식을 제거한다.
3. CLAUDE.md를 위 구조로 재작성한다. README.md와 겹치는 섹션 삭제.
4. GEMINI.md를 `MUST READ CLAUDE.md.` 한 줄로 교체한다.
5. docs/ 파일을 삭제한다.
6. 커밋 후 서브모듈 포인터를 루트에서 업데이트한다.
