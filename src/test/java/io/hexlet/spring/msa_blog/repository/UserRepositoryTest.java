package io.hexlet.spring.msa_blog.repository;

import io.hexlet.spring.msa_blog.model.User;
import io.hexlet.spring.msa_blog.util.ModelGenerator;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Sql(statements = {
        "DELETE FROM comments",
        "DELETE FROM posts",
        "DELETE FROM users"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static Long savedUserId;
    private static String savedUserEmail;

    @Test
    @Order(1)
    @Rollback(false)
    void testSave() {
        User user = ModelGenerator.buildUser();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(saved.getLastName()).isEqualTo(user.getLastName());
        assertThat(saved.getEmail()).isEqualTo(user.getEmail());

        savedUserId = saved.getId();
        savedUserEmail = saved.getEmail();
    }

    @Test
    @Order(2)
    void testFindById() {
        Optional<User> found = userRepository.findById(savedUserId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUserId);
        assertThat(found.get().getEmail()).isEqualTo(savedUserEmail);
    }

    @Test
    @Order(3)
    void testFindAll() {
        Iterable<User> all = userRepository.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(4)
    @Rollback(false)
    void testUpdate() {
        User user = userRepository.findById(savedUserId).orElseThrow();
        String newFirstName = "UpdatedFirst";
        String newLastName = "UpdatedLast";
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);

        userRepository.save(user);

        User updated = userRepository.findById(savedUserId).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo(newFirstName);
        assertThat(updated.getLastName()).isEqualTo(newLastName);
        assertThat(updated.getEmail()).isEqualTo(savedUserEmail);
    }

    @Test
    @Order(5)
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail(savedUserEmail);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUserId);
        assertThat(found.get().getEmail()).isEqualTo(savedUserEmail);
    }

    @Test
    @Order(6)
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    @Order(7)
    @Rollback(false)
    void testDelete() {
        userRepository.deleteById(savedUserId);

        Optional<User> deleted = userRepository.findById(savedUserId);
        assertThat(deleted).isEmpty();
    }
}
