package io.akeyless.secrets;

import com.google.common.collect.ImmutableList;
import io.airlift.spi.secrets.SecretProviderFactory;
import io.airlift.spi.secrets.SecretsPlugin;

import java.util.List;

public class AkeylessSecretsPlugin
        implements SecretsPlugin
{
    @Override
    public List<SecretProviderFactory> getSecretProviderFactories()
    {
        return ImmutableList.of(new AkeylessSecretProviderFactory());
    }
}
