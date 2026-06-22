package io.akeyless.secrets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestAkeylessClientPaths
{
    @Test
    void normalizesLeadingSlash()
    {
        assertThat(AkeylessClient.normalizeItemPath("prod/db")).isEqualTo("/prod/db");
        assertThat(AkeylessClient.normalizeItemPath("/prod/db")).isEqualTo("/prod/db");
    }

    @Test
    void resolvesRelativePathAgainstBase()
    {
        assertThat(AkeylessClient.resolveSecretPath("/starburst", "catalogs/postgres"))
                .isEqualTo("/starburst/catalogs/postgres");
    }

    @Test
    void absolutePathIgnoresBase()
    {
        assertThat(AkeylessClient.resolveSecretPath("/starburst", "/absolute/path"))
                .isEqualTo("/absolute/path");
    }

    @Test
    void rejectsEmptySecretPath()
    {
        assertThatThrownBy(() -> AkeylessClient.resolveSecretPath("/base", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
