package io.akeyless.secrets;

import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigSecuritySensitive;
import jakarta.validation.constraints.NotNull;

public class AkeylessSecretProviderConfig
{
    private String apiUrl = "https://api.akeyless.io";
    private String authType = "access_key";
    private String accessId;
    private String accessKey;
    private String apiToken;
    private String jwt;
    private String cloudId;
    private String gcpAudience;
    private String secretBasePath;
    private String secretType = "static";

    @Config("api-url")
    public AkeylessSecretProviderConfig setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
        return this;
    }

    @NotNull
    public String getApiUrl()
    {
        return apiUrl;
    }

    @Config("auth-type")
    public AkeylessSecretProviderConfig setAuthType(String authType)
    {
        this.authType = authType;
        return this;
    }

    @NotNull
    public String getAuthType()
    {
        return authType;
    }

    public AuthenticationType getAuthenticationType()
    {
        return AuthenticationType.fromConfig(authType);
    }

    @Config("access-id")
    public AkeylessSecretProviderConfig setAccessId(String accessId)
    {
        this.accessId = accessId;
        return this;
    }

    public String getAccessId()
    {
        return accessId;
    }

    @Config("access-key")
    @ConfigSecuritySensitive
    public AkeylessSecretProviderConfig setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
        return this;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    @Config("api-token")
    @ConfigSecuritySensitive
    public AkeylessSecretProviderConfig setApiToken(String apiToken)
    {
        this.apiToken = apiToken;
        return this;
    }

    public String getApiToken()
    {
        return apiToken;
    }

    @Config("jwt")
    @ConfigSecuritySensitive
    public AkeylessSecretProviderConfig setJwt(String jwt)
    {
        this.jwt = jwt;
        return this;
    }

    public String getJwt()
    {
        return jwt;
    }

    @Config("cloud-id")
    public AkeylessSecretProviderConfig setCloudId(String cloudId)
    {
        this.cloudId = cloudId;
        return this;
    }

    public String getCloudId()
    {
        return cloudId;
    }

    @Config("gcp-audience")
    public AkeylessSecretProviderConfig setGcpAudience(String gcpAudience)
    {
        this.gcpAudience = gcpAudience;
        return this;
    }

    public String getGcpAudience()
    {
        return gcpAudience;
    }

    @Config("secret-base-path")
    public AkeylessSecretProviderConfig setSecretBasePath(String secretBasePath)
    {
        this.secretBasePath = secretBasePath;
        return this;
    }

    public String getSecretBasePath()
    {
        return secretBasePath;
    }

    @Config("secret-type")
    public AkeylessSecretProviderConfig setSecretType(String secretType)
    {
        this.secretType = secretType;
        return this;
    }

    @NotNull
    public String getSecretType()
    {
        return secretType;
    }

    public SecretType getDefaultSecretType()
    {
        return SecretType.fromConfig(secretType);
    }
}
