# Next.js 프론트엔드 인프라 배치 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Next.js BFF를 프라이빗 서브넷에 EC2로 배치하고, Nginx가 모든 외부 트래픽을 BFF로 라우팅하도록 Terraform 인프라를 구성한다.

**Architecture:** NAT/Ingress 노드의 Nginx가 모든 HTTP 트래픽을 Next.js BFF(10.0.101.50:3000)로 라우팅한다. BFF는 프라이빗 서브넷에서 API Gateway(10.0.101.200:8080)를 직접 호출한다. API Gateway 보안 그룹은 기존 nat_ingress SG 대신 frontend SG 출처만 허용한다.

**Tech Stack:** Terraform ~> 5.0 (AWS provider), Amazon Linux 2023 ARM64, Docker Compose, Grafana Alloy

---

## 파일 구조

### 수정 대상

- `infrastructure/layers/01-base/security.tf` - frontend SG 추가, API Gateway SG 인바운드 규칙 변경
- `infrastructure/layers/01-base/secrets.tf` - bff_jwt_secret 추가
- `infrastructure/layers/01-base/variables.tf` - frontend_private_ip, bff_jwt_secret_key 변수 추가
- `infrastructure/layers/01-base/outputs.tf` - frontend SG ID, frontend IP, 신규 시크릿 이름 output 추가
- `infrastructure/layers/02-network-services/nat_ingress.tf` - Nginx 템플릿에 frontend_ip 전달
- `infrastructure/layers/02-network-services/templates/nginx.conf.tftpl` - upstream을 frontend로 변경

### 신규 생성 대상

- `infrastructure/layers/06-apps-frontend/providers.tf`
- `infrastructure/layers/06-apps-frontend/data.tf`
- `infrastructure/layers/06-apps-frontend/variables.tf`
- `infrastructure/layers/06-apps-frontend/frontend.tf`
- `infrastructure/layers/06-apps-frontend/templates/config.alloy.tftpl`
- `infrastructure/layers/06-apps-frontend/templates/user_data.sh.tftpl`
- `infrastructure/layers/06-apps-frontend/templates/docker-compose.frontend.yaml.tftpl`

---

## Task 1: 01-base 레이어 변경

**Files:**
- Modify: `infrastructure/layers/01-base/security.tf`
- Modify: `infrastructure/layers/01-base/secrets.tf`
- Modify: `infrastructure/layers/01-base/variables.tf`
- Modify: `infrastructure/layers/01-base/outputs.tf`

### 보안 그룹 및 시크릿 변경

- [ ] **Step 1: frontend 보안 그룹 추가 및 API Gateway SG 인바운드 규칙 변경**

`infrastructure/layers/01-base/security.tf`의 `aws_security_group.api_gateway` 인바운드 규칙에서 `nat_ingress`를 제거하고 `frontend`로 교체한다. 파일 맨 아래에 frontend SG를 추가한다.

```hcl
# API Gateway Security Group - 기존 ingress 규칙 변경
resource "aws_security_group" "api_gateway" {
  name        = "quietchatter-api-gateway-sg"
  description = "Security group for API Gateway"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 80
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.frontend.id]  # nat_ingress -> frontend
  }

  ingress {
    from_port   = 8301
    to_port     = 8301
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  ingress {
    from_port   = 8301
    to_port     = 8301
    protocol    = "udp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "quietchatter-api-gateway-sg"
  }
}
```

파일 맨 아래에 추가:

```hcl
# Frontend (Next.js BFF) Security Group
resource "aws_security_group" "frontend" {
  name        = "quietchatter-frontend-sg"
  description = "Security group for Next.js BFF"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 3000
    to_port         = 3000
    protocol        = "tcp"
    security_groups = [aws_security_group.nat_ingress.id]
  }

  ingress {
    from_port   = 8301
    to_port     = 8301
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  ingress {
    from_port   = 8301
    to_port     = 8301
    protocol    = "udp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "quietchatter-frontend-sg"
  }
}
```

- [ ] **Step 2: BFF JWT 시크릿 추가**

`infrastructure/layers/01-base/secrets.tf` 파일 맨 아래에 추가:

```hcl
# BFF JWT Secret Key (Next.js BFF session management)
resource "aws_secretsmanager_secret" "bff_jwt_secret_key" {
  name        = "quietchatter-bff-jwt-secret-key"
  description = "JWT signing secret key for Next.js BFF session cookies"

  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "bff_jwt_secret_key" {
  secret_id     = aws_secretsmanager_secret.bff_jwt_secret_key.id
  secret_string = var.bff_jwt_secret_key
}
```

- [ ] **Step 3: 변수 추가**

`infrastructure/layers/01-base/variables.tf` 파일 맨 아래에 추가:

```hcl
variable "frontend_private_ip" {
  description = "Static private IP for the Next.js BFF Node"
  type        = string
  default     = "10.0.101.50"
}

variable "bff_jwt_secret_key" {
  description = "JWT signing secret key for Next.js BFF session cookies"
  type        = string
  sensitive   = true
  default     = ""
}
```

- [ ] **Step 4: output 추가**

`infrastructure/layers/01-base/outputs.tf` 파일 맨 아래에 추가:

```hcl
output "frontend_sg_id" {
  value = aws_security_group.frontend.id
}

output "frontend_private_ip" {
  value = var.frontend_private_ip
}

output "bff_jwt_secret_name" {
  value = aws_secretsmanager_secret.bff_jwt_secret_key.name
}

output "naver_client_id_secret_name" {
  value = aws_secretsmanager_secret.naver_client_id.name
}

output "naver_client_secret_secret_name" {
  value = aws_secretsmanager_secret.naver_client_secret.name
}
```

- [ ] **Step 5: terraform validate 실행**

```bash
cd infrastructure/layers/01-base
terraform validate
```

Expected: `Success! The configuration is valid.`

- [ ] **Step 6: terraform plan 검토**

```bash
cd infrastructure/layers/01-base
terraform plan
```

Expected plan 확인 항목:
- `aws_security_group.frontend` 신규 생성
- `aws_security_group.api_gateway` ingress 규칙 변경 (nat_ingress -> frontend)
- `aws_secretsmanager_secret.bff_jwt_secret_key` 신규 생성

- [ ] **Step 7: terraform apply**

```bash
cd infrastructure/layers/01-base
terraform apply
```

- [ ] **Step 8: 커밋**

```bash
git add infrastructure/layers/01-base/
git commit -m "feat(infra): add frontend SG and BFF JWT secret to 01-base"
```

---

## Task 2: 06-apps-frontend 레이어 신규 생성

**Files:**
- Create: `infrastructure/layers/06-apps-frontend/providers.tf`
- Create: `infrastructure/layers/06-apps-frontend/data.tf`
- Create: `infrastructure/layers/06-apps-frontend/variables.tf`
- Create: `infrastructure/layers/06-apps-frontend/frontend.tf`
- Create: `infrastructure/layers/06-apps-frontend/templates/config.alloy.tftpl`
- Create: `infrastructure/layers/06-apps-frontend/templates/user_data.sh.tftpl`
- Create: `infrastructure/layers/06-apps-frontend/templates/docker-compose.frontend.yaml.tftpl`

- [ ] **Step 1: providers.tf 생성**

```hcl
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}
```

- [ ] **Step 2: data.tf 생성**

```hcl
data "terraform_remote_state" "base" {
  backend = "local"
  config = {
    path = "../01-base/terraform.tfstate"
  }
}

data "terraform_remote_state" "platform" {
  backend = "local"
  config = {
    path = "../03-platform/terraform.tfstate"
  }
}
```

- [ ] **Step 3: variables.tf 생성**

```hcl
variable "aws_region" {
  description = "The AWS region to deploy the infrastructure"
  type        = string
  default     = "ap-northeast-2"
}

variable "ami_id" {
  description = "The AMI ID to use for EC2 instances (Amazon Linux 2023 ARM64)"
  type        = string
  default     = "ami-0e31683998cedb019"
}

variable "frontend_image" {
  description = "Docker image for the Next.js BFF"
  type        = string
  default     = "maskun2/quietchatter-frontend:latest"
}
```

- [ ] **Step 4: config.alloy.tftpl 생성**

`infrastructure/layers/06-apps-frontend/templates/config.alloy.tftpl`:

```
logging {
  level = "info"
}

// System Logs (journald)
loki.relabel "journal" {
  forward_to = [loki.write.grafana_cloud.receiver]

  rule {
    source_labels = ["__journal__systemd_unit"]
    target_label  = "unit"
  }
}

loki.source.journal "read" {
  forward_to    = [loki.relabel.journal.receiver]
  relabel_rules = loki.relabel.journal.rules
  labels        = { 
    job          = "quietchatter/system",
    instance     = "${instance_name}",
    service_name = "system",
  }
}

// Docker Discovery & Relabeling
discovery.docker "linux" {
  host = "unix:///var/run/docker.sock"
}

discovery.relabel "docker" {
  targets = discovery.docker.linux.targets

  rule {
    source_labels = ["__meta_docker_container_name"]
    regex         = "/(.*)"
    replacement   = "$1"
    target_label  = "service_name"
  }

  rule {
    target_label = "instance"
    replacement  = "${instance_name}"
  }
  
  rule {
    target_label = "job"
    replacement  = "quietchatter/docker"
  }
}

loki.source.docker "logs" {
  host       = "unix:///var/run/docker.sock"
  targets    = discovery.relabel.docker.output
  forward_to = [loki.write.grafana_cloud.receiver]
}

loki.write "grafana_cloud" {
  endpoint {
    url = "${loki_url}"

    basic_auth {
      username = "${loki_user}"
      password = sys.env("GRAFANA_API_KEY")
    }
  }
}
```

- [ ] **Step 5: docker-compose.frontend.yaml.tftpl 생성**

`infrastructure/layers/06-apps-frontend/templates/docker-compose.frontend.yaml.tftpl`:

```yaml
version: '3.8'
services:
  consul-agent:
    image: hashicorp/consul:1.14
    container_name: quietchatter-consul-agent
    restart: always
    environment:
      CONSUL_BIND_INTERFACE: ens5
    command: "agent -join=${controlplane_ip} -data-dir=/consul/data -client=0.0.0.0"
    network_mode: host
    deploy:
      resources:
        limits:
          memory: 100M

  frontend:
    image: ${service_image}
    container_name: quietchatter-frontend
    restart: always
    environment:
      - NODE_ENV=production
      - NEXT_TELEMETRY_DISABLED=1
      - INTERNAL_API_GATEWAY_URL=http://${api_gateway_ip}:8080
      - BFF_JWT_SECRET=$BFF_JWT_SECRET
      - NAVER_CLIENT_ID=$NAVER_CLIENT_ID
      - NAVER_CLIENT_SECRET=$NAVER_CLIENT_SECRET
    ports:
      - "3000:3000"
    network_mode: host
    deploy:
      resources:
        limits:
          memory: 512M

  watchtower:
    image: containrrr/watchtower
    container_name: quietchatter-watchtower
    restart: always
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      - WATCHTOWER_POLL_INTERVAL=120
      - WATCHTOWER_CLEANUP=true
      - WATCHTOWER_INCLUDE_RESTARTING=true
      - WATCHTOWER_REVIVE_STOPPED=true
    deploy:
      resources:
        limits:
          memory: 100M
```

- [ ] **Step 6: user_data.sh.tftpl 생성**

`infrastructure/layers/06-apps-frontend/templates/user_data.sh.tftpl`:

```bash
#!/bin/bash
set -e

log() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')] $1"
}

error_handler() {
  log "ERROR: $1 단계에서 문제가 발생했습니다. 작업을 중단합니다."
  exit 1
}

log "INFO: userdata 스크립트 실행을 시작합니다."

log "STEP 0: 인터넷 연결을 확인하고 있습니다..."
until ping -c 1 8.8.8.8 > /dev/null 2>&1; do
  log "인터넷 연결 대기 중 (5초 후 재시도)..."
  sleep 5
done
log "인터넷 연결 확인 완료."

log "STEP 1: AWS Secrets Manager에서 비밀 정보를 가져오고 있습니다..."
{
  GRAFANA_API_KEY=$(aws secretsmanager get-secret-value --region ${aws_region} --secret-id ${grafana_api_key_secret_name} --query SecretString --output text)
  BFF_JWT_SECRET=$(aws secretsmanager get-secret-value --region ${aws_region} --secret-id ${bff_jwt_secret_name} --query SecretString --output text)
  NAVER_CLIENT_ID=$(aws secretsmanager get-secret-value --region ${aws_region} --secret-id ${naver_client_id_secret_name} --query SecretString --output text)
  NAVER_CLIENT_SECRET=$(aws secretsmanager get-secret-value --region ${aws_region} --secret-id ${naver_client_secret_secret_name} --query SecretString --output text)
} || error_handler "시크릿 정보 가져오기"

{
  cat <<EOF > /home/ec2-user/.env
GRAFANA_API_KEY=$GRAFANA_API_KEY
BFF_JWT_SECRET=$BFF_JWT_SECRET
NAVER_CLIENT_ID=$NAVER_CLIENT_ID
NAVER_CLIENT_SECRET=$NAVER_CLIENT_SECRET
EOF
  chown ec2-user:ec2-user /home/ec2-user/.env
  chmod 600 /home/ec2-user/.env
} || error_handler ".env 파일 생성"
log ".env 파일 생성 및 권한 설정 완료."

log "STEP 2: Swap 파일을 생성하고 시스템 설정을 진행합니다..."
{
  dd if=/dev/zero of=/swapfile bs=128M count=16
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  if ! grep -q "/swapfile" /etc/fstab; then
    echo "/swapfile swap swap defaults 0 0" >> /etc/fstab
  fi
} || error_handler "Swap 설정"

log "STEP 3: Docker 및 Docker Compose를 설치하고 있습니다..."
{
  dnf clean all
  dnf install docker -y || { log "Docker 설치 재시도 중..."; sleep 5; dnf install docker -y; }
  systemctl enable docker
  systemctl start docker
  mkdir -p /usr/local/lib/docker/cli-plugins/
  curl -SL https://github.com/docker/compose/releases/download/v2.26.1/docker-compose-linux-aarch64 -o /usr/local/lib/docker/cli-plugins/docker-compose
  chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
} || error_handler "Docker 설치"

log "STEP 4: Grafana Alloy를 설치하고 설정을 적용하고 있습니다..."
{
  cat << 'REPO' > /etc/yum.repos.d/grafana.repo
[grafana]
name=grafana
baseurl=https://rpm.grafana.com
repo_gpgcheck=1
enabled=1
gpgcheck=1
gpgkey=https://rpm.grafana.com/gpg.key
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
REPO

  dnf install alloy -y
  usermod -aG docker alloy

  cat <<'EOF' > /etc/alloy/config.alloy
${alloy_config}
EOF

  echo "GRAFANA_API_KEY=$GRAFANA_API_KEY" >> /etc/sysconfig/alloy
  systemctl enable alloy
  systemctl start alloy
} || error_handler "Grafana Alloy 설정"

log "STEP 5: Docker Compose 설정 파일을 생성하고 있습니다..."
{
  cat <<'EOF' > /home/ec2-user/docker-compose.yaml
${docker_compose_config}
EOF
} || error_handler "설정 파일 생성"

log "STEP 6: 서비스를 실행합니다..."
{
  cd /home/ec2-user
  source .env
  docker compose up -d
} || error_handler "서비스 실행"

log "INFO: 모든 userdata 설정이 성공적으로 완료되었습니다."
```

- [ ] **Step 7: frontend.tf 생성**

```hcl
locals {
  alloy_config = templatefile("${path.module}/templates/config.alloy.tftpl", {
    instance_name = "quietchatter-frontend-node"
    loki_url      = data.terraform_remote_state.base.outputs.grafana_cloud_logs_url
    loki_user     = data.terraform_remote_state.base.outputs.grafana_cloud_user
  })

  docker_compose_config = templatefile("${path.module}/templates/docker-compose.frontend.yaml.tftpl", {
    controlplane_ip = data.terraform_remote_state.platform.outputs.controlplane_private_ip
    service_image   = var.frontend_image
    api_gateway_ip  = data.terraform_remote_state.base.outputs.api_gateway_private_ip
  })
}

resource "aws_instance" "frontend" {
  ami           = var.ami_id
  instance_type = "t4g.micro"
  subnet_id     = data.terraform_remote_state.base.outputs.private_subnet_ids[0]
  private_ip    = data.terraform_remote_state.base.outputs.frontend_private_ip

  vpc_security_group_ids = [data.terraform_remote_state.base.outputs.frontend_sg_id]
  iam_instance_profile   = data.terraform_remote_state.base.outputs.ssm_profile_name

  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/templates/user_data.sh.tftpl", {
    aws_region                    = var.aws_region
    grafana_api_key_secret_name   = data.terraform_remote_state.base.outputs.grafana_api_key_secret_name
    bff_jwt_secret_name           = data.terraform_remote_state.base.outputs.bff_jwt_secret_name
    naver_client_id_secret_name   = data.terraform_remote_state.base.outputs.naver_client_id_secret_name
    naver_client_secret_secret_name = data.terraform_remote_state.base.outputs.naver_client_secret_secret_name
    alloy_config                  = local.alloy_config
    docker_compose_config         = local.docker_compose_config
  })

  tags = {
    Name = "quietchatter-frontend-node"
  }
}

output "frontend_private_ip" {
  value = aws_instance.frontend.private_ip
}
```

- [ ] **Step 8: terraform init 실행**

```bash
cd infrastructure/layers/06-apps-frontend
terraform init
```

Expected: `Terraform has been successfully initialized!`

- [ ] **Step 9: terraform validate 실행**

```bash
cd infrastructure/layers/06-apps-frontend
terraform validate
```

Expected: `Success! The configuration is valid.`

- [ ] **Step 10: terraform plan 검토**

```bash
cd infrastructure/layers/06-apps-frontend
terraform plan
```

Expected plan 확인 항목:
- `aws_instance.frontend` 신규 생성 (subnet: private[0], private_ip: 10.0.101.50, sg: frontend_sg)

- [ ] **Step 11: terraform apply**

```bash
cd infrastructure/layers/06-apps-frontend
terraform apply
```

- [ ] **Step 12: EC2 기동 확인 (약 3분 소요)**

```bash
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=quietchatter-frontend-node" \
  --query "Reservations[].Instances[].{State:State.Name,IP:PrivateIpAddress}" \
  --output table
```

Expected: State=running, IP=10.0.101.50

- [ ] **Step 13: 커밋**

```bash
git add infrastructure/layers/06-apps-frontend/
git commit -m "feat(infra): add 06-apps-frontend layer for Next.js BFF"
```

---

## Task 3: 02-network-services Nginx 라우팅 변경

이 Task는 Task 2가 완료되어 Next.js BFF 인스턴스가 실행 중인 상태에서 수행한다. Nginx 설정을 변경하면 즉시 외부 트래픽이 Next.js로 라우팅되므로, Next.js 컨테이너가 정상 기동된 것을 확인한 후 진행한다.

**Files:**
- Modify: `infrastructure/layers/02-network-services/templates/nginx.conf.tftpl`
- Modify: `infrastructure/layers/02-network-services/nat_ingress.tf`

- [ ] **Step 1: nginx.conf.tftpl 수정**

`infrastructure/layers/02-network-services/templates/nginx.conf.tftpl`의 내용을 아래로 교체:

```nginx
user  nginx;
worker_processes  auto;

error_log  /var/log/nginx/error.log notice;
pid        /var/run/nginx.pid;

events {
    worker_connections  1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    keepalive_timeout  65;

    # Upstream to Next.js BFF
    upstream frontend {
        server ${frontend_ip}:3000;
    }

    server {
        listen       80;
        server_name  localhost;

        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

- [ ] **Step 2: nat_ingress.tf의 nginx_config 인자 변경**

`infrastructure/layers/02-network-services/nat_ingress.tf`에서 nginx_config 템플릿 인자를 수정한다.

변경 전:
```hcl
nginx_config = templatefile("${path.module}/templates/nginx.conf.tftpl", {
  api_gateway_ip  = data.terraform_remote_state.base.outputs.api_gateway_private_ip
})
```

변경 후:
```hcl
nginx_config = templatefile("${path.module}/templates/nginx.conf.tftpl", {
  frontend_ip = data.terraform_remote_state.base.outputs.frontend_private_ip
})
```

- [ ] **Step 3: terraform validate 실행**

```bash
cd infrastructure/layers/02-network-services
terraform validate
```

Expected: `Success! The configuration is valid.`

- [ ] **Step 4: terraform plan 검토**

```bash
cd infrastructure/layers/02-network-services
terraform plan
```

Expected plan 확인 항목:
- `aws_instance.nat_ingress` user_data 변경 (nginx upstream이 frontend로 변경됨)
- `user_data_replace_on_change = true`이므로 인스턴스 교체 발생 예정

- [ ] **Step 5: terraform apply**

```bash
cd infrastructure/layers/02-network-services
terraform apply
```

NAT 인스턴스가 교체되므로 약 2-3분간 인터넷 연결 및 외부 접근이 일시 중단된다.

- [ ] **Step 6: 라우팅 확인**

NAT 인스턴스 교체 완료 후 (약 3분), 퍼블릭 IP를 조회하여 외부에서 HTTP 요청을 보내 Next.js BFF로 라우팅되는지 확인한다.

```bash
NAT_PUBLIC_IP=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=quietchatter-nat-ingress-node" "Name=instance-state-name,Values=running" \
  --query "Reservations[].Instances[].PublicIpAddress" \
  --output text)

curl -v http://$NAT_PUBLIC_IP/
```

Expected: Next.js BFF의 응답 (Next.js가 실행 중이면 HTML 또는 JSON 응답)

- [ ] **Step 7: 커밋**

```bash
git add infrastructure/layers/02-network-services/
git commit -m "feat(infra): route all HTTP traffic to Next.js BFF via Nginx"
```
