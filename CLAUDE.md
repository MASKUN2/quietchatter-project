# CLAUDE.md

작업 전 README.md를 읽으십시오. 제품 정의, 아키텍처 원칙, 기술 스택, API 경로, 배포 방식, 작업 이력은 README.md에 있습니다.

작업 대상 서비스의 CLAUDE.md도 읽으십시오.

## 빌드 및 테스트 명령

각 마이크로서비스 폴더에서 실행합니다.

```
./gradlew build
./gradlew test
./gradlew test --tests "com.quietchatter.{service}.{패키지}.{클래스명}"
./gradlew bootRun
./gradlew testDocs
./gradlew openapi3
./gradlew bootJar
```

프론트엔드 폴더에서 실행합니다.

```
npm install
npm run dev
npm run build
npm run lint
```

## 핵심 작업 규칙

서비스 탐색: Consul 사용 금지. k8s DNS(service.namespace.svc.cluster.local) 사용.

레거시 서브모듈: 읽기 전용. 파일 수정 및 커밋 절대 금지. 로직 참조 시 idiomatic Kotlin으로 재작성.

문서 작성: 강조 서식(굵게, 기울임) 및 이모티콘 사용 절대 금지. 평문(Plain Text)만 사용.

AI 의사결정 원칙:
- 업계 표준 기술 우선 선택 (실험적 기술 지양)
- 여러 대안 나열 대신 단일 최적안을 제안
- 기술 근거는 간결하게, 실행 가능한 결론 중심으로 보고
