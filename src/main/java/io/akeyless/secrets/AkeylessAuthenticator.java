package io.akeyless.secrets;

import io.akeyless.client.ApiException;
import io.akeyless.client.model.Auth;
import io.akeyless.cloudid.CloudIdProvider;
import io.akeyless.cloudid.CloudProviderFactory;

final class AkeylessAuthenticator
{
    private AkeylessAuthenticator() {}

    static Auth buildAuth(AkeylessSecretProviderConfig config)
            throws ApiException
    {
        AuthenticationType type = config.getAuthenticationType();
        return switch (type) {
            case API_TOKEN -> throw new ApiException("api-token is used directly; no auth request is required");
            case ACCESS_KEY -> buildAccessKeyAuth(config);
            case JWT -> buildJwtAuth(config);
            case AWS_IAM -> buildCloudAuth(config, "aws_iam");
            case AZURE_AD -> buildCloudAuth(config, "azure_ad");
            case GCP -> buildGcpAuth(config);
        };
    }

    private static Auth buildAccessKeyAuth(AkeylessSecretProviderConfig config)
            throws ApiException
    {
        String accessId = trim(config.getAccessId());
        String accessKey = normalizeAccessKey(config.getAccessKey());
        if (accessId == null || accessId.isEmpty() || accessKey.isEmpty()) {
            throw new ApiException("access-id and access-key are required for access_key authentication");
        }
        Auth auth = new Auth();
        auth.setAccessId(accessId);
        auth.setAccessKey(accessKey);
        auth.setAccessType("access_key");
        return auth;
    }

    private static Auth buildJwtAuth(AkeylessSecretProviderConfig config)
            throws ApiException
    {
        String accessId = trim(config.getAccessId());
        String jwt = trim(config.getJwt());
        if (accessId == null || accessId.isEmpty() || jwt == null || jwt.isEmpty()) {
            throw new ApiException("access-id and jwt are required for jwt authentication");
        }
        Auth auth = new Auth();
        auth.setAccessId(accessId);
        auth.setAccessType("jwt");
        auth.setJwt(jwt);
        return auth;
    }

    private static Auth buildCloudAuth(AkeylessSecretProviderConfig config, String accessType)
            throws ApiException
    {
        String accessId = trim(config.getAccessId());
        if (accessId == null || accessId.isEmpty()) {
            throw new ApiException("access-id is required for " + accessType + " authentication");
        }
        String cloudId = resolveCloudId(accessType, config.getCloudId());
        Auth auth = new Auth();
        auth.setAccessId(accessId);
        auth.setAccessType(accessType);
        auth.setCloudId(cloudId);
        return auth;
    }

    private static Auth buildGcpAuth(AkeylessSecretProviderConfig config)
            throws ApiException
    {
        Auth auth = buildCloudAuth(config, "gcp");
        String audience = trim(config.getGcpAudience());
        if (audience != null && !audience.isEmpty()) {
            auth.setGcpAudience(audience);
        }
        return auth;
    }

    private static String resolveCloudId(String accessType, String configuredCloudId)
            throws ApiException
    {
        String cloudId = trim(configuredCloudId);
        if (cloudId != null && !cloudId.isEmpty()) {
            return cloudId;
        }
        try {
            CloudIdProvider provider = CloudProviderFactory.getCloudIdProvider(accessType);
            return provider.getCloudId();
        }
        catch (Exception e) {
            throw new ApiException("Could not obtain cloud identity for " + accessType
                    + ". Run on the matching cloud platform or set cloud-id explicitly. "
                    + e.getMessage());
        }
    }

    static String normalizeAccessKey(String raw)
    {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", "");
    }

    private static String trim(String value)
    {
        return value == null ? null : value.trim();
    }
}
