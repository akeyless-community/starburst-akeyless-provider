package io.akeyless.secrets;

import java.util.Locale;
import java.util.Optional;

/**
 * Parses secret reference keys from configuration properties.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>{@code /path/to/secret}</li>
 *   <li>{@code /path/to/secret:jsonKey}</li>
 *   <li>{@code dynamic:/path/to/producer:password}</li>
 *   <li>{@code rotated:/path/to/secret:username}</li>
 * </ul>
 */
final class SecretReference
{
    private final SecretType secretType;
    private final String secretPath;
    private final String jsonKey;

    private SecretReference(SecretType secretType, String secretPath, String jsonKey)
    {
        this.secretType = secretType;
        this.secretPath = secretPath;
        this.jsonKey = jsonKey;
    }

    SecretType getSecretType()
    {
        return secretType;
    }

    String getSecretPath()
    {
        return secretPath;
    }

    Optional<String> getJsonKey()
    {
        return Optional.ofNullable(jsonKey);
    }

    static SecretReference parse(String key, SecretType defaultSecretType)
    {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Secret reference must not be empty");
        }

        String remainder = key.trim();
        SecretType secretType = defaultSecretType;

        int typeSeparator = remainder.indexOf(':');
        if (typeSeparator > 0) {
            String possibleType = remainder.substring(0, typeSeparator).trim().toLowerCase(Locale.ROOT);
            if (isSecretTypePrefix(possibleType)) {
                secretType = SecretType.fromConfig(possibleType);
                remainder = remainder.substring(typeSeparator + 1).trim();
            }
        }

        int jsonKeySeparator = remainder.lastIndexOf(':');
        if (jsonKeySeparator > 0 && jsonKeySeparator < remainder.length() - 1) {
            String path = remainder.substring(0, jsonKeySeparator).trim();
            String jsonKey = remainder.substring(jsonKeySeparator + 1).trim();
            if (!path.isEmpty() && !jsonKey.isEmpty()) {
                return new SecretReference(secretType, path, jsonKey);
            }
        }

        return new SecretReference(secretType, remainder, null);
    }

    private static boolean isSecretTypePrefix(String value)
    {
        return switch (value) {
            case "static", "dynamic", "rotated" -> true;
            default -> false;
        };
    }
}
