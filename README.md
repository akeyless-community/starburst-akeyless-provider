# Starburst Akeyless Secrets Provider

Native [Akeyless](https://www.akeyless.io/) secrets provider plugin for [Starburst Enterprise Platform (SEP)](https://docs.starburst.io/latest/security/secrets.html).

Resolves configuration references like `${akeyless:/path/to/secret:jsonKey}` at runtime using the Akeyless REST API.

Built on the Airlift `SecretProviderFactory` SPI (same model as SEP's built-in keystore provider).

## Build

Requires Java 21+ and Maven 3.9+.

```bash
cd starburst-akeyless-secrets-provider
mvn clean package
```

Artifacts:

- `target/starburst-akeyless-secrets-provider-1.0.0.jar` — plugin classes
- `target/starburst-akeyless-secrets-provider-1.0.0.zip` — deployable plugin bundle (copy into `secrets-plugins-dir`)

Built against Airlift `395` / `secrets-spi`. Match `dep.airlift.version` in `pom.xml` to the Airlift version bundled with your SEP release when upgrading.

See [docs/TESTING.md](docs/TESTING.md) for how to validate without a local SEP cluster.

## Install on SEP

1. Copy the plugin ZIP into your SEP secrets plugins directory:

```bash
unzip target/starburst-akeyless-secrets-provider-1.0.0.zip -d /usr/lib/starburst/secrets-plugins/
```

2. Create or update `/etc/secrets.toml`:

```toml
secrets-plugins-dir="/usr/lib/starburst/secrets-plugins"

[akeyless]
secrets-provider.name = "akeyless"
api-url = "https://api.akeyless.io"
auth-type = "access_key"
access-id = "p-xxxxxxxx"
access-key = "your-access-key"
secret-base-path = "/starburst"
secret-type = "static"
```

3. Reference secrets in catalog or server configuration:

```properties
connector.name=postgresql
connection-url=jdbc:postgresql://postgresql:5432/analytics
connection-user=${akeyless:catalogs/postgres:user}
connection-password=${akeyless:catalogs/postgres:password}
```

With `secret-base-path = "/starburst"`, the example above resolves `/starburst/catalogs/postgres` from Akeyless.

## Secret reference syntax

| Reference | Meaning |
|-----------|---------|
| `${akeyless:/my/secret}` | Full static secret value |
| `${akeyless:/my/secret:password}` | JSON field `password` from static secret |
| `${akeyless:dynamic:/prod/mysql:password}` | Dynamic secret producer |
| `${akeyless:rotated:/prod/db:user}` | Rotated secret JSON field |

The section name in `secrets.toml` (e.g. `[akeyless_prod]`) is what you use in property files. The `secrets-provider.name` must be `akeyless`.

## Configuration properties

| Property | Required | Description |
|----------|----------|-------------|
| `secrets-provider.name` | Yes | Must be `akeyless` |
| `api-url` | No | Akeyless API URL (default: `https://api.akeyless.io`) |
| `auth-type` | No | `access_key` (default), `api_token`, `jwt`, `aws_iam`, `azure_ad`, `gcp` |
| `access-id` | For most auth types | Akeyless Access ID |
| `access-key` | For `access_key` | Akeyless Access Key |
| `api-token` | For `api_token` | Pre-issued Akeyless token (`t-...`) |
| `jwt` | For `jwt` | JWT for OIDC/JWT auth method |
| `cloud-id` | Optional | Explicit cloud identity for IAM auth |
| `gcp-audience` | Optional | GCP audience for GCP auth |
| `secret-base-path` | No | Prefix prepended to relative secret paths |
| `secret-type` | No | Default type: `static`, `dynamic`, or `rotated` |

## Multiple provider instances

```toml
[akeyless_prod]
secrets-provider.name = "akeyless"
api-url = "https://api.akeyless.io"
auth-type = "access_key"
access-id = "p-prod"
access-key = "prod-key"
secret-base-path = "/starburst/prod"

[akeyless_dev]
secrets-provider.name = "akeyless"
api-url = "https://api.akeyless.io"
auth-type = "access_key"
access-id = "p-dev"
access-key = "dev-key"
secret-base-path = "/starburst/dev"
```

```properties
connection-user=${akeyless_prod:catalogs/postgres:user}
connection-password=${akeyless_dev:catalogs/postgres:password}
```

## SEP version compatibility

This plugin uses the Airlift `SecretProviderFactory` SPI (`io.airlift.spi.secrets`).

- Default build targets Airlift **395** (compatible with recent SEP releases that ship the secrets framework).
- Before deploying to production, confirm your SEP version includes the Airlift secrets SPI and align `dep.airlift.version` if needed.
- Runtime requires Java 22+ (provided by SEP). Build works on Java 21.

## Gateway URL

To route through an Akeyless Gateway instead of SaaS API:

```toml
api-url = "https://your-gateway.example.com/api/v2"
```

## Security notes

- Store `access-key`, `api-token`, and `jwt` in environment-specific secret stores or mounted files, not in plain Git.
- Prefer cloud IAM auth (`aws_iam`, `azure_ad`, `gcp`) on SEP nodes running in the matching cloud.
- Use narrow Akeyless RBAC policies scoped to the paths referenced by SEP catalogs.
