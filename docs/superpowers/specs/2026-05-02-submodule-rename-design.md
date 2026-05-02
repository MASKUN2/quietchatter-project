# 설계 문서: 서브모듈 네이밍 및 경로 변경 (frontend)

이 문서는 `legacy-quiet-chatter-front-end` 서브모듈을 더 현대적이고 간결한 `frontend`로 변경하기 위한 설계를 기술합니다.

## 1. 변경 목적
* "Legacy" 명칭을 제거하여 프로젝트의 현대화 의지를 반영
* 길고 복잡한 경로를 `frontend`로 단일화하여 접근성 향상
* 변경된 원격 저장소(GitHub) URL 반영

## 2. 변경 명세
| 항목 | 기존 (Old) | 변경 (New) |
| :--- | :--- | :--- |
| **서브모듈 이름** | legacy-quiet-chatter-front-end | frontend |
| **로컬 경로** | legacy-quiet-chatter-front-end/ | frontend/ |
| **원격 URL** | https://github.com/MASKUN2/quiet-chatter-front-end | https://github.com/MASKUN2/quietchatter-frontend |

## 3. 구현 단계
1. **파일 시스템 이동**: `git mv` 명령어를 사용하여 폴더 이동 및 Git 인덱스 업데이트
2. **설정 수정**: `.gitmodules` 파일 내의 섹션 이름 및 URL 수동 업데이트
3. **동기화**: `git submodule sync` 명령어로 변경된 URL을 로컬 Git 설정에 반영
4. **검증**: `git status` 및 서브모듈 내부 파일 접근 확인

## 4. 영향도 평가
* **코드 참조**: `grep` 검색 결과, 소스 코드 내에서 기존 경로를 문자열로 참조하는 부분은 발견되지 않음
* **빌드 스크립트**: 현재 루트 디렉토리의 Gradle 설정 등에서 해당 경로를 직접 참조하는지 실행 시 재확인 필요
