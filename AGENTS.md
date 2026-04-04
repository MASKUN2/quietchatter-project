# AI Agent Guide - Root Module

이 문서는 AI 에이전트가 QuietChatter 프로젝트를 이해하고 안전하게 작업을 수행하기 위한 통합 지침입니다.

## 1. 프로젝트 개요

- 목적: 레거시 모놀리식 시스템을 현대적인 마이크로서비스 아키텍처(MSA)로 전환.
- 주요 구성: Gateway, Member, Book, Talk, Customer 마이크로서비스 및 Terraform 인프라.

## 2. 에이전트 초기 작업 절차

모든 작업을 시작하기 전 다음 문서를 반드시 순서대로 읽고 컨텍스트를 파악해야 합니다.

1. docs/requirements/prd.md: 제품의 목적과 기능적 요구사항 파악.
2. docs/ARCHITECTURE.md: 시스템 설계 원칙 및 전체 서비스 구조 파악.
3. docs/DEVELOPMENT.md: 코딩 표준, 기술 규약, EDA 설계 및 AI 의사결정 원칙 파악.
4. docs/HISTORY.md: 최근 변경 사항 및 현재 진행 중인 과제 확인.

## 3. 핵심 행동 지침

- 문서 스타일 준수: 모든 문서 작성 및 답변 시 강조 서식(굵게, 기울임 등)과 이모티콘 사용을 절대 금지합니다. 오직 평문(Plain Text)만 사용합니다.
- 의사결정 원칙: 업계 표준 기술을 우선 선정하며, 여러 대안 대신 단일 최적 경로(Single Path)를 제안합니다.
- 기술 스택: Kotlin 1.9.x, Spring Boot 3.5.13, Java 21(Virtual Threads), Redpanda(Kafka), Consul을 기본으로 합니다.
- 헥사고날 아키텍처: 모든 마이크로서비스는 헥사고날 아키텍처를 따르며, 어댑터 패키지 명칭은 adaptor로 통일합니다.

## 4. 서브모듈 작업 시 주의사항

각 서브모듈(microservice-*, infrastructure) 내부에 별도의 AGENTS.md가 존재하는 경우, 해당 도메인에 특화된 지침을 추가로 확인해야 합니다.

## 5. 스프린트 기반 AI 에이전트 워크플로우

프로젝트의 요구사항 변경에 유연하게 대처하고 에이전트 컨텍스트를 최적화하기 위해 스프린트 기반 워크플로우를 사용합니다.

- Planning: 루트의 docs/requirements/prd.md 는 수시로 변경되는 요구사항 문서입니다.
- Architecting & Management: 루트의 docs/sprints/버전/ 폴더 아래의 spec.md 와 plan.md 로 각 스프린트 목표를 고정합니다.
- Development: 각 마이크로서비스 폴더의 docs/tasks/버전-task.md 문서를 바탕으로 개발 및 단위 테스트를 수행합니다.
- Review & Sync: 서브모듈 작업 완료 후, 사양 변경이 필요했다면 역으로 루트의 스프린트 문서 및 PRD를 업데이트합니다.
