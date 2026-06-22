# Testing without Starburst Enterprise Platform

You can validate most of the plugin **before** a customer SEP cluster is available.

## 1. Automated unit tests (no Akeyless account required)

```bash
mvn clean test
```

Covers secret reference parsing, path normalization, and provider naming.

## 2. Build the deployable plugin bundle

```bash
mvn clean package
ls -lh target/starburst-akeyless-secrets-provider-*.zip
```

Confirm the ZIP contains the plugin JAR, `META-INF/services/io.airlift.spi.secrets.SecretsPlugin`, and runtime dependencies.

## 3. Live Akeyless API smoke test (no SEP)

Verify credentials and secret paths the same way the plugin does:

```bash
export AKEYLESS_ACCESS_ID="p-xxxxxxxx"
export AKEYLESS_ACCESS_KEY="your-access-key"
export AKEYLESS_API_URL="https://api.akeyless.io"
export AKEYLESS_SECRET_PATH="/starburst/catalogs/postgres"

./scripts/smoke-test-akeyless.sh
```

This authenticates and fetches the configured secret path. Fix auth/RBAC/path issues here before involving SEP.

## 4. Provider resolution smoke test (Java, no SEP)

With the same env vars, exercise the plugin's `SecretProvider` directly:

```bash
export AKEYLESS_SECRET_KEY="password"   # JSON field to extract, optional

mvn -q exec:java \
  -Dexec.mainClass=io.akeyless.secrets.SmokeTestMain \
  -Dexec.classpathScope=test
```

This bootstraps `AkeylessSecretProviderFactory` the same way SEP does at runtime.

## 5. Customer SEP validation (required before GA)

The final proof is on a customer or internal SEP cluster:

1. Copy `target/starburst-akeyless-secrets-provider-*.zip` into `secrets-plugins-dir`.
2. Add `[akeyless]` section to `/etc/secrets.toml` (see `examples/secrets.toml`).
3. Reference secrets in a catalog, e.g. `connection-password=${akeyless:catalogs/postgres:password}`.
4. Restart coordinator and workers; confirm catalog loads and queries succeed.

### Minimal customer POC checklist

| Step | Pass criteria |
|------|----------------|
| Plugin loads | SEP starts without secrets-provider errors in logs |
| Static secret | Catalog property resolves from Akeyless path |
| JSON key | `${akeyless:path:key}` returns expected field |
| Dynamic secret | `${akeyless:dynamic:producer:password}` works for DB catalog |
| Auth method | Customer's chosen auth (access key / IAM / JWT) succeeds |
| RBAC | SEP service account can only read allowed paths |

## 6. SEP access options for your team

If you do not run SEP locally:

- **Customer lab** — run the POC checklist on the customer's non-prod SEP cluster (recommended).
- **Starburst evaluation** — request an evaluation license / trial environment from Starburst for partner engineering.
- **Internal QA cluster** — deploy SEP on Kubernetes using Starburst Helm charts in a shared QA account.

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| `UnsupportedClassVersionError` at runtime | SEP JVM older than plugin's Airlift dependency; align `dep.airlift.version` in `pom.xml` |
| Secret not found | Wrong path, missing leading slash in Akeyless, or `secret-base-path` mismatch |
| Auth failed | Invalid access key, or cloud IAM auth not running on matching cloud |
| Plugin not loaded | ZIP not extracted into `secrets-plugins-dir`, or `secrets-provider.name` ≠ `akeyless` |
