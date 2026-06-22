package io.akeyless.secrets;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.spi.secrets.SecretProvider;
import io.airlift.spi.secrets.SecretProviderFactory;

import java.util.Map;

import static io.airlift.configuration.ConfigBinder.configBinder;

public class AkeylessSecretProviderFactory
        implements SecretProviderFactory
{
    public static final String PROVIDER_NAME = "akeyless";

    @Override
    public String getName()
    {
        return PROVIDER_NAME;
    }

    @Override
    public SecretProvider createSecretProvider(Map<String, String> config)
    {
        Bootstrap app = new Bootstrap(
                binder -> {
                    configBinder(binder).bindConfig(AkeylessSecretProviderConfig.class);
                    binder.bind(SecretProvider.class).to(AkeylessSecretProvider.class).in(Scopes.SINGLETON);
                });

        Injector injector = app
                .doNotInitializeLogging()
                .setRequiredConfigurationProperties(config)
                .initialize();

        return injector.getInstance(SecretProvider.class);
    }
}
