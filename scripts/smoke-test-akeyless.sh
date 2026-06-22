#!/usr/bin/env bash
set -euo pipefail

: "${AKEYLESS_ACCESS_ID:?Set AKEYLESS_ACCESS_ID}"
: "${AKEYLESS_ACCESS_KEY:?Set AKEYLESS_ACCESS_KEY}"
: "${AKEYLESS_SECRET_PATH:?Set AKEYLESS_SECRET_PATH}"

API_URL="${AKEYLESS_API_URL:-https://api.akeyless.io}"
JSON_KEY="${AKEYLESS_SECRET_KEY:-}"

echo "Authenticating to ${API_URL}..."
TOKEN=$(curl -sfS -X POST "${API_URL}/auth" \
  -H 'Content-Type: application/json' \
  -d "{\"access-id\":\"${AKEYLESS_ACCESS_ID}\",\"access-key\":\"${AKEYLESS_ACCESS_KEY}\",\"access-type\":\"access_key\"}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo "Fetching secret ${AKEYLESS_SECRET_PATH}..."
RESPONSE=$(curl -sfS -X POST "${API_URL}/get-secret-value" \
  -H 'Content-Type: application/json' \
  -d "{\"token\":\"${TOKEN}\",\"names\":[\"${AKEYLESS_SECRET_PATH}\"]}")

if [[ -n "${JSON_KEY}" ]]; then
  echo "${RESPONSE}" | python3 -c "
import sys, json
data = json.load(sys.stdin)
path = '${AKEYLESS_SECRET_PATH}'
key = '${JSON_KEY}'
raw = data.get(path) or next(iter(data.values()))
obj = json.loads(raw) if isinstance(raw, str) and raw.strip().startswith('{') else raw
if isinstance(obj, dict):
    print(obj[key])
else:
    print(raw)
"
else
  echo "${RESPONSE}"
fi

echo "Smoke test OK."
