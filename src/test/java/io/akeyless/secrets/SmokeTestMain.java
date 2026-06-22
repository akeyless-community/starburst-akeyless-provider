package io.akeyless.secrets;

import io.airlift.spi.secrets.SecretProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Manual smoke test — run against a live Akeyless tenant without SEP.
 *
 * <pre>
 * export AKEYLESS_ACCESS_ID=...
 * export AKEYLESS_ACCESS_KEY=...
 * export AKEYLESS_SECRET_PATH=catalogs/postgres:password
 * mvn -q exec:java -Dexec.mainClass=io.akeyless.secrets.SmokeTestMain -Dexec.classpathScope=test
 * </pre>
 */
public final class SmokeTestMain
{
    private SmokeTestMain() {}

    public static void main(String[] args)
    {
        String accessId = env("AKEYLESS_ACCESS_ID");
        String accessKey = env("AKEYLESS_ACCESS_KEY");
        String apiUrl = envOrDefault("AKEYLESS_API_URL", "https://api.akeyless.io");
        String secretPath = env("AKEYLESS_SECRET_PATH");
        String basePath = envOrDefault("AKEYLESS_SECRET_BASE_PATH", "/starburst");

        Map<String, String> config = new HashMap<>();
        config.put("api-url", apiUrl);
        config.put("auth-type", "access_key");
        config.put("access-id", accessId);
        config.put("access-key", accessKey);
        config.put("secret-base-path", basePath);
        config.put("secret-type", "static");

        SecretProvider provider = new AkeylessSecretProviderFactory().createSecretProvider(config);
        String value = provider.resolveSecretValue(secretPath);
        System.out.println("Resolved secret (length=" + value.length() + ")");
    }

    private static String env(String name)
    {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return value.trim();
    }

    private static String envOrDefault(String name, String defaultValue)
    {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
