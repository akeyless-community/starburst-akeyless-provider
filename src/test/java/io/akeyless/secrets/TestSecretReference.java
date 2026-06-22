package io.akeyless.secrets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestSecretReference
{
    @Test
    void parsesPlainPath()
    {
        SecretReference reference = SecretReference.parse("/prod/db/password", SecretType.STATIC);

        assertThat(reference.getSecretType()).isEqualTo(SecretType.STATIC);
        assertThat(reference.getSecretPath()).isEqualTo("/prod/db/password");
        assertThat(reference.getJsonKey()).isEmpty();
    }

    @Test
    void parsesPathWithJsonKey()
    {
        SecretReference reference = SecretReference.parse("/prod/db/creds:password", SecretType.STATIC);

        assertThat(reference.getSecretPath()).isEqualTo("/prod/db/creds");
        assertThat(reference.getJsonKey()).contains("password");
    }

    @Test
    void parsesDynamicPrefix()
    {
        SecretReference reference = SecretReference.parse("dynamic:/prod/mysql-producer:password", SecretType.STATIC);

        assertThat(reference.getSecretType()).isEqualTo(SecretType.DYNAMIC);
        assertThat(reference.getSecretPath()).isEqualTo("/prod/mysql-producer");
        assertThat(reference.getJsonKey()).contains("password");
    }

    @Test
    void parsesRotatedPrefix()
    {
        SecretReference reference = SecretReference.parse("rotated:/prod/db/user:username", SecretType.STATIC);

        assertThat(reference.getSecretType()).isEqualTo(SecretType.ROTATED);
        assertThat(reference.getSecretPath()).isEqualTo("/prod/db/user");
        assertThat(reference.getJsonKey()).contains("username");
    }

    @Test
    void rejectsEmptyReference()
    {
        assertThatThrownBy(() -> SecretReference.parse("  ", SecretType.STATIC))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
