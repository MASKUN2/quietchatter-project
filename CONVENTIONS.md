# 프로젝트 및 서브모듈 관리 규칙 (Project and Submodule Conventions)

이 문서는 quietchatter-project 저장소 내의 폴더, 프로젝트 명명 규칙, Git 서브모듈 관리 방식 및 문서 작성 규칙을 정의합니다.

## Git 서브모듈 관리 규칙 (Git Submodule Management)

본 프로젝트는 마이크로서비스 아키텍처를 기반으로 하며, 각 서비스 및 인프라 코드는 독립적인 저장소로 관리되는 Git 서브모듈 방식입니다.

1. 서브모듈 추가: 새로운 도메인이나 기능이 추가될 때 반드시 서브모듈로 등록합니다.
2. 명명 규칙: 서브모듈 폴더명은 아래의 [마이크로서비스 폴더 명명 규칙] 및 [레거시 모듈 명명 규칙]을 따릅니다.
3. 업데이트 원칙: 세션 중 서브모듈의 추가, 삭제 또는 경로 변경이 발생할 경우 즉시 이 문서(CONVENTIONS.md)를 업데이트하여 최신 구조를 유지합니다.

## 마이크로서비스 폴더 명명 규칙

본 프로젝트의 신규 마이크로서비스 저장소(서브모듈) 폴더명은 다음과 같은 규칙을 따릅니다:

형식: microservice-<domain-name>

예시:
* microservice-gateway (API Gateway: 라우팅 및 공통 인증)
* microservice-user (회원 및 보안 도메인)
* microservice-book (도서 도메인)
* microservice-talk (대화 및 반응 도메인)
* microservice-customer (고객 지원 도메인)
* infrastructure (공통 인프라 설정 저장소)

## 레거시 모듈 명명 규칙 (Legacy Module Naming)

기존 시스템 분석 및 참조를 위해 추가된 레거시 저장소(서브모듈) 폴더명은 다음과 같은 규칙을 따릅니다:

형식: legacy-<repository-name>

예시:
* legacy-quiet-chatter
* legacy-quiet-chatter-front-end
* legacy-quiet-chatter-batch
* legacy-quiet-chatter-docs

### 규칙 채택 이유 및 장점

1. 소문자 사용: Git 및 다양한 운영체제(Windows, Mac, Linux) 환경에서 대소문자 구분으로 인한 충돌을 방지하고 일관되게 동작합니다.
2. 케밥 케이스(kebab-case) 사용: 단어 사이에 하이픈(-)을 사용하여 가독성을 높였습니다.
3. 일관된 접두사(Prefix): 신규 서비스는 microservice-, 레거시 서비스는 legacy- 접두사를 사용하여 프로젝트의 성격을 명확히 구분하고 알파벳 순으로 정렬하여 관리하기 쉽습니다.

## 문서 작성 규칙 (Documentation Style)

이 프로젝트의 모든 마크다운 파일 작성 및 답변 시 다음 규칙을 엄격히 준수합니다.

1. 강조 서식 사용 금지: 굵게(bold)나 기울임(italics) 서식을 절대 사용하지 않습니다.
2. 이모티콘 사용 금지: 문서 및 답변 내에서 이모티콘을 사용하지 않습니다.
3. 명확한 문장 사용: 가독성을 위해 간결하고 명확한 문장을 사용합니다.
