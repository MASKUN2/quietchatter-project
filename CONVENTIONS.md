# 프로젝트 네이밍 규칙 (Project Naming Conventions)

이 문서는 `quiet-chatter-project` 저장소 내의 폴더 및 프로젝트 명명 규칙을 정의합니다.

## 마이크로서비스 폴더 명명 규칙

본 프로젝트의 마이크로서비스 저장소(서브모듈) 폴더명은 다음과 같은 규칙을 따릅니다:

**형식:** `microservice-<domain-name>`

**예시:**
* `microservice-book`
* `microservice-user`

### 규칙 채택 이유 및 장점

1. **소문자 사용**: Git 및 다양한 운영체제(Windows, Mac, Linux) 환경에서 대소문자 구분으로 인한 충돌을 방지하고 일관되게 동작합니다.
2. **케밥 케이스(kebab-case) 사용**: 단어 사이에 하이픈(`-`)을 사용하여 가독성을 높였습니다. 이는 URL 경로나 GitHub 저장소 이름으로 가장 널리 쓰이는 표준 방식입니다.
3. **일관된 접두사(Prefix)**: 모든 서비스가 `microservice-` 접두사로 시작하므로, 루트 디렉토리에 다른 종류의 폴더(예: `docs/`, `scripts/`, `common-lib/`)가 추가되더라도 서비스 관련 폴더들이 알파벳 순으로 깔끔하게 그룹화되어 관리하기 쉽습니다.
