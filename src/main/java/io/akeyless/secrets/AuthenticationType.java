package io.akeyless.secrets;

public enum AuthenticationType
{
    ACCESS_KEY,
    API_TOKEN,
    JWT,
    AWS_IAM,
    AZURE_AD,
    GCP;

    public static AuthenticationType fromConfig(String value)
    {
        if (value == null || value.isBlank()) {
            return ACCESS_KEY;
        }
        return switch (value.trim().toLowerCase()) {
            case "access_key", "api_key" -> ACCESS_KEY;
            case "api_token", "token" -> API_TOKEN;
            case "jwt" -> JWT;
            case "aws_iam", "aws" -> AWS_IAM;
            case "azure_ad", "azure" -> AZURE_AD;
            case "gcp", "google" -> GCP;
            default -> throw new IllegalArgumentException("Unsupported auth-type: " + value);
        };
    }
}
