package io.hexlet.spring.msa_blog;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest {
    private DriverManagerDataSource database() {
        return new DriverManagerDataSource("jdbc:h2:mem:migration_" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    private Flyway flyway(DriverManagerDataSource database, boolean baseline) {
        return Flyway.configure().dataSource(database)
                .locations("classpath:db/migration/structure", "classpath:db/migration/data")
                .baselineOnMigrate(baseline).baselineVersion("1").cleanDisabled(true).load();
    }

    @Test
    void emptyDatabaseIsCreatedSeededAndNotMigratedTwice() {
        var database = database();
        var flyway = flyway(database, false);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        var jdbc = new JdbcTemplate(database);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM posts", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM comments", Integer.class)).isEqualTo(1);
        jdbc.update("UPDATE posts SET body = 'User edit' WHERE slug = 'welcome-to-blog'");
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        flyway.validate();
        assertThat(jdbc.queryForObject("SELECT body FROM posts WHERE slug = 'welcome-to-blog'", String.class))
                .isEqualTo("User edit");
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("1.1");
    }

    @Test
    void adoptingExistingSchemaPreservesExistingRecords() {
        var database = database();
        new ResourceDatabasePopulator(new ClassPathResource(
                "db/migration/structure/V1__create_blog_tables.sql")).execute(database);
        var jdbc = new JdbcTemplate(database);
        jdbc.update("INSERT INTO users (first_name,last_name,email,password) VALUES (?,?,?,?)",
                "Existing", "User", "existing@example.com", "existing-password");
        var flyway = flyway(database, true);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT first_name FROM users WHERE email = 'existing@example.com'",
                String.class)).isEqualTo("Existing");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM posts", Integer.class)).isEqualTo(3);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        flyway.validate();
    }
}
