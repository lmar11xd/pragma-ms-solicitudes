package co.com.bancolombia.r2dbc.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlConnectionPropertiesTest {

    @Test
    void shouldCreateRecordWithValues() {
        PostgresqlConnectionProperties props = new PostgresqlConnectionProperties(
                "localhost",
                5432,
                "mydb",
                "public",
                "user",
                "secret"
        );

        assertThat(props.host()).isEqualTo("localhost");
        assertThat(props.port()).isEqualTo(5432);
        assertThat(props.database()).isEqualTo("mydb");
        assertThat(props.schema()).isEqualTo("public");
        assertThat(props.username()).isEqualTo("user");
        assertThat(props.password()).isEqualTo("secret");
    }
}