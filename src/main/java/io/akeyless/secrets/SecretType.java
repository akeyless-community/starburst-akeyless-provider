package io.akeyless.secrets;

public enum SecretType
{
    STATIC,
    DYNAMIC,
    ROTATED;

    public static SecretType fromConfig(String value)
    {
        if (value == null || value.isBlank()) {
            return STATIC;
        }
        return switch (value.trim().toLowerCase()) {
            case "static" -> STATIC;
            case "dynamic" -> DYNAMIC;
            case "rotated" -> ROTATED;
            default -> throw new IllegalArgumentException("Unsupported secret-type: " + value);
        };
    }
}
