package io.akeyless.secrets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestAkeylessSecretProviderFactory
{
    @Test
    void providerNameMatchesSecretsToml()
    {
        assertThat(AkeylessSecretProviderFactory.PROVIDER_NAME).isEqualTo("akeyless");
    }
}
