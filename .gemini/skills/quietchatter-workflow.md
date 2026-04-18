# Skill: QuietChatter Sprint Workflow

본 기술은 QuietChatter 프로젝트의 스프린트 기반 개발 및 관리 사이클을 정의합니다.

## 1. 개요
요구사항 정의부터 구현, 테스트, 문서 동기화까지의 표준 단계를 강제하여 MSA 환경에서의 정렬(Alignment)을 유지합니다.

## 2. 작업 단계 (Execution Steps)

### 1단계: 설계 고정 (Architecting)
- 루트의 docs/sprints/ 해당 버전 폴더에서 spec.md(사양)와 plan.md(계획)를 작성하거나 확인합니다.
- 변경이 필요한 경우 PRD(docs/requirements/prd.md)와 대조하여 일관성을 검토합니다.

### 2단계: 태스크 정의 (Tasking)
- 대상 마이크로서비스 폴더 내의 docs/tasks/버전-task.md 파일을 생성하거나 업데이트합니다.
- 구현해야 할 기능과 테스트 케이스를 명확히 기술합니다.

### 3단계: 구현 및 검증 (Implementation & Validation)
- 헥사고날 아키텍처 원칙(adaptor 패키지명 준수 등)에 따라 코드를 작성합니다.
- Spring RestDocs를 활용하여 API 스펙을 자동 생성하고 테스트를 통과시킵니다.

### 4단계: 역동기화 (Reverse Sync)
- 개발 과정에서 사양이나 아키텍처의 변경이 발생했다면, 루트의 스프린트 문서와 PRD를 최신화합니다.
- HISTORY.md에 주요 변경 내용을 기록합니다.

## 3. 금기 사항 (Anti-Patterns)
- PRD 확인 없이 구현을 시작하는 행위.
- 헥사고날 아키텍처를 무시하고 레이어드 아키텍처로 작성하는 행위.
- 답변 시 강조 서식이나 이모티콘을 사용하는 행위.
