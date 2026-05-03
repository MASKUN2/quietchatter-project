#!/bin/bash
set -euo pipefail

log() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')] $1"
}

source /etc/infra-asset-config

export KUBECONFIG=/home/ec2-user/.kube/config

# 0. sync.sh 자기 자신 갱신
SELF=/home/ec2-user/sync.sh
TMP_SELF=$(mktemp)
aws s3 cp "s3://$S3_BUCKET/$S3_PATH_PREFIX/sync.sh" "$TMP_SELF" --region "$AWS_REGION"
if ! diff -q "$TMP_SELF" "$SELF" > /dev/null 2>&1; then
  cp "$TMP_SELF" "$SELF"
  chown ec2-user:ec2-user "$SELF"
  chmod +x "$SELF"
  rm "$TMP_SELF"
  log "INFO: sync.sh가 갱신되었습니다. 재실행합니다."
  exec "$SELF"
fi
rm "$TMP_SELF"

# 1. k3s API 준비 확인
log "STEP 1: k3s API 상태를 확인합니다..."
until kubectl get nodes > /dev/null 2>&1; do
  log "k3s API 준비 대기 중..."
  sleep 5
done

# 2. 노드 role 라벨 부여 (quietchatter.io/role → node-role.kubernetes.io/*)
log "STEP 2: 노드 role 라벨을 부여합니다..."
for role in controlplane gateway worker; do
  kubectl label node -l quietchatter.io/role=$role \
    node-role.kubernetes.io/$role=true --overwrite 2>/dev/null || true
done

# 3. k8s Secret 생성/갱신 (Secrets Manager → k8s Secret)
log "STEP 3: AWS Secrets Manager에서 시크릿을 동기화합니다..."
SECRETS=$(aws secretsmanager get-secret-value \
  --region "$AWS_REGION" --secret-id "quietchatter-secrets" \
  --query 'SecretString' --output text)

DB_PASSWORD=$(echo "$SECRETS" | jq -r '.db_password')
DB_USERNAME=$(echo "$SECRETS" | jq -r '.db_username')
GRAFANA_API_KEY=$(echo "$SECRETS" | jq -r '.grafana_api_key')
LOKI_URL=$(echo "$SECRETS" | jq -r '.loki_url')
LOKI_USER=$(echo "$SECRETS" | jq -r '.loki_user')
NAVER_CLIENT_ID=$(echo "$SECRETS" | jq -r '.naver_client_id')
NAVER_CLIENT_SECRET=$(echo "$SECRETS" | jq -r '.naver_client_secret')
JWT_SECRET_KEY=$(echo "$SECRETS" | jq -r '.jwt_secret_key')
INTERNAL_SECRET=$(echo "$SECRETS" | jq -r '.internal_secret')

kubectl create namespace quietchatter --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic quietchatter-secrets \
  --namespace=quietchatter \
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \
  --from-literal=DB_USERNAME="$DB_USERNAME" \
  --from-literal=GRAFANA_API_KEY="$GRAFANA_API_KEY" \
  --from-literal=LOKI_URL="$LOKI_URL" \
  --from-literal=LOKI_USER="$LOKI_USER" \
  --from-literal=NAVER_CLIENT_ID="$NAVER_CLIENT_ID" \
  --from-literal=NAVER_CLIENT_SECRET="$NAVER_CLIENT_SECRET" \
  --from-literal=JWT_SECRET_KEY="$JWT_SECRET_KEY" \
  --from-literal=INTERNAL_SECRET="$INTERNAL_SECRET" \
  --dry-run=client -o yaml | kubectl apply -f -

log "INFO: 시크릿 동기화 완료."

# 4. Ghost Node 정리 (10분 이상 NotReady인 노드 삭제)
log "STEP 4: Ghost Node를 정리합니다..."
THRESHOLD_SEC=600
kubectl get nodes -o json | jq -r --argjson t "$THRESHOLD_SEC" '
  .items[] |
  select(.metadata.labels["quietchatter.io/role"] == "worker") |
  select(
    .status.conditions[] |
    select(.type == "Ready" and .status == "False")
  ) |
  select(
    (now - (
      .status.conditions[] |
      select(.type == "Ready") |
      .lastTransitionTime | fromdateiso8601
    )) > $t
  ) |
  .metadata.name
' | while read -r node; do
  if [ -n "$node" ]; then
    log "INFO: Ghost Node 삭제 시도 - $node"
    kubectl delete node "$node" --ignore-not-found
  fi
done

# 5. S3에서 매니페스트 동기화 후 apply
log "STEP 5: S3에서 k8s 매니페스트를 동기화하고 적용합니다..."
MANIFEST_DIR=/home/ec2-user/manifests
mkdir -p "$MANIFEST_DIR"
aws s3 sync "s3://$S3_BUCKET/$S3_PATH_PREFIX/manifests/" "$MANIFEST_DIR/" \
  --region "$AWS_REGION" --delete --exact-timestamps

DISCORD_WEBHOOK="https://discord.com/api/webhooks/1485990859798352005/o0Ccdv9FUDe3w0zScFhjN8GVqdMV6eLF83S8RNtqtik-1JwcVbd4bc3Y9VWsGfzftLml"

APPLY_OUTPUT=$(kubectl apply -f "$MANIFEST_DIR/" --recursive)
echo "$APPLY_OUTPUT"

echo "$APPLY_OUTPUT" | grep "deployment.apps/" | grep "configured" | awk '{print $1}' | sed 's|deployment.apps/||' | while read -r deployment; do
  NAMESPACE=$(kubectl get deployment -A --field-selector "metadata.name=$deployment" -o jsonpath='{.items[0].metadata.namespace}' 2>/dev/null)
  IMAGE_TAG=$(kubectl get deployment "$deployment" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].image}' 2>/dev/null | awk -F: '{print $2}')

  if kubectl rollout status deployment/"$deployment" -n "$NAMESPACE" --timeout=300s > /dev/null 2>&1; then
    curl -s -X POST "$DISCORD_WEBHOOK" \
      -H 'Content-type: application/json' \
      -d "{\"content\":\"[운영] $deployment 배포 완료. ($IMAGE_TAG)\"}"
  else
    curl -s -X POST "$DISCORD_WEBHOOK" \
      -H 'Content-type: application/json' \
      -d "{\"content\":\"[운영] $deployment 배포 실패. ($IMAGE_TAG)\"}"
  fi
done

log "INFO: 매니페스트 적용 완료."
log "INFO: 동기화 완료."
