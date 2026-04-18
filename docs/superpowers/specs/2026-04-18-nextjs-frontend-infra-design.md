# Next.js 프론트엔드 인프라 배치 설계

작성일: 2026-04-18

## 1. 개요

Next.js BFF(Backend for Frontend)를 프라이빗 서브넷에 배치하고, 기존 NAT/Ingress 노드의 Nginx가 모든 HTTP 트래픽을 Next.js로 라우팅하는 구조. SSR 및 OAuth2 세션 관리를 담당하며, 향후 EKS 전환 시 서브넷 변경 없이 파드로 교체 가능하도록 설계한다.

## 2. 트래픽 흐름

```
인터넷
  |
Nginx (NAT/Ingress 노드, 퍼블릭 서브넷)
  |  /* 모든 요청
Next.js BFF (프라이빗 서브넷, EC2)
  |  X-Member-Id 헤더 주입
API Gateway (프라이빗 서브넷, EC2)
  |  보안 그룹: BFF SG 출처 트래픽만 허용
마이크로서비스들 (프라이빗 서브넷, ASG)
```

클라이언트는 Next.js BFF만 알고, API Gateway와 마이크로서비스는 인터넷에 노출하지 않는다.

## 3. 인증 흐름

### OAuth2 최초 로그인

1. 브라우저가 BFF의 OAuth2 시작 엔드포인트에 접근
2. BFF가 OAuth 프로바이더로 리다이렉트
3. 프로바이더가 BFF 콜백 URL로 식별자와 프로바이더 정보 전달
4. BFF가 microservice-member에 식별자와 프로바이더 정보를 전달하여 회원가입 또는 로그인 처리
5. microservice-member가 서비스 memberId 반환
6. BFF가 memberId를 포함한 JWT를 발급하여 httpOnly 쿠키로 브라우저에 전달

### 이후 API 호출

1. 브라우저가 httpOnly 쿠키 포함 요청을 BFF로 전송
2. BFF가 쿠키의 JWT를 파싱하여 memberId 추출
3. BFF가 X-Member-Id 헤더를 주입하여 API Gateway 호출
4. API Gateway는 보안 그룹 격리에 의존하고 X-Member-Id 헤더를 신뢰
5. API Gateway가 해당 헤더를 마이크로서비스로 전파

X-Internal-Secret 헤더는 현재 사용하지 않는다. 보안 그룹으로 BFF에서 오는 트래픽만 허용하여 네트워크 레벨 격리를 확보한다. 필요 시 나중에 추가한다.

## 4. 컴포넌트 역할

### Next.js BFF

- SSR 처리
- OAuth2 콜백 수신 및 세션 관리 (httpOnly 쿠키)
- JWT 발급 및 파싱
- 모든 API 요청을 API Gateway로 프록시하며 X-Member-Id 헤더 주입

### API Gateway (Spring Cloud Gateway)

- BFF에서 오는 요청의 X-Member-Id 헤더를 신뢰하여 마이크로서비스로 라우팅
- JWT 검증 책임 없음 (BFF가 담당)
- 보안 그룹으로 BFF 외 출처 차단

### microservice-member

- OAuth2 식별자와 프로바이더 정보를 받아 회원가입 또는 로그인 처리
- 서비스 memberId 반환

## 5. Terraform 계층 변경

### 01-base (보안 그룹 추가)

- Next.js BFF용 보안 그룹 신규 생성
- API Gateway 보안 그룹 인바운드 규칙에 BFF SG 출처 허용 추가

### 02-network-services (Nginx 설정 변경)

- 기존: 모든 HTTP 트래픽을 API Gateway 프라이빗 IP로 라우팅
- 변경: 모든 HTTP 트래픽을 Next.js BFF 프라이빗 IP로 라우팅

### 06-apps-frontend (신규 레이어)

- Next.js BFF EC2 인스턴스 (프라이빗 서브넷)
- 인스턴스 타입: t4g.micro
- SSM IAM 프로파일 연결 (API 직접 테스트 시 SSM 포트 포워딩 사용)

## 6. 직접 API 테스트 방법

API Gateway 인스턴스에 SSM 포트 포워딩으로 직접 접근한다.

```bash
aws ssm start-session \
  --target {api-gateway-instance-id} \
  --document-name AWS-StartPortForwardingSession \
  --parameters '{"portNumber":["8080"],"localPortNumber":["18080"]}'
```

이후 Postman 등에서 localhost:18080으로 X-Member-Id 헤더를 포함하여 테스트한다.

## 7. EKS 전환 경로

Next.js를 처음부터 프라이빗 서브넷에 배치하므로 EKS 전환 시 서브넷 변경이 없다.

```
현재
  Nginx (EC2, 퍼블릭) -> Next.js BFF (EC2, 프라이빗) -> API Gateway (EC2, 프라이빗)
  NAT Instance가 프라이빗 서브넷의 아웃바운드 트래픽 처리

EKS 전환 후
  ALB (퍼블릭, AWS 관리) -> Next.js BFF 파드 (프라이빗) -> API Gateway 파드 (프라이빗)
  NAT Gateway로 교체
```

NAT/Ingress 노드의 역할(Nginx 리버스 프록시 + NAT)은 ALB + Ingress Controller + NAT Gateway로 분리된다.
