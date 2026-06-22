package io.akeyless.secrets;

import com.google.inject.Inject;
import io.airlift.spi.secrets.SecretProvider;
import io.akeyless.client.ApiException;

public class AkeylessSecretProvider
        implements SecretProvider
{
    private final AkeylessSecretProviderConfig config;
    private final AkeylessClient client;

    @Inject
    public AkeylessSecretProvider(AkeylessSecretProviderConfig config)
    {
        this.config = config;
        this.client = new AkeylessClient(config);
    }

    @Override
    public String resolveSecretValue(String key)
    {
        try {
            SecretReference reference = SecretReference.parse(key, config.getDefaultSecretType());
            String secretPath = AkeylessClient.resolveSecretPath(config.getSecretBasePath(), reference.getSecretPath());
            return client.getSecretValue(
                    secretPath,
                    reference.getJsonKey().orElse(null),
                    reference.getSecretType());
        }
        catch (ApiException e) {
            throw new RuntimeException("Failed to resolve Akeyless secret for key '" + key + "': " + e.getMessage(), e);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to resolve Akeyless secret for key '" + key + "'", e);
        }
    }
}
