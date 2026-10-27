package io.hexlet.spring.msa_blog.repository;

import io.hexlet.spring.msa_blog.model.Post;
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
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private static Long savedUserId;
    private static Long savedPostId;
    private static String savedPostSlug;

    @Test
    @Order(1)
    @Rollback(false)
    void testSaveUserFirst() {
        User user = ModelGenerator.buildUser();
        User savedUser = userRepository.save(user);
        savedUserId = savedUser.getId();
        assertThat(savedUserId).isGreaterThan(0);
    }

    @Test
    @Order(2)
    @Rollback(false)
    void testSave() {
        User author = userRepository.findById(savedUserId).orElseThrow();
        Post post = ModelGenerator.buildPost(author);

        Post saved = postRepository.save(post);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getName()).isEqualTo(post.getName());
        assertThat(saved.getSlug()).isEqualTo(post.getSlug());
        assertThat(saved.getBody()).isEqualTo(post.getBody());
        assertThat(saved.getAuthor().getId()).isEqualTo(savedUserId);

        savedPostId = saved.getId();
        savedPostSlug = saved.getSlug();
    }

    @Test
    @Order(3)
    void testFindById() {
        Optional<Post> found = postRepository.findById(savedPostId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedPostId);
        assertThat(found.get().getSlug()).isEqualTo(savedPostSlug);
    }

    @Test
    @Order(4)
    void testFindAll() {
        Iterable<Post> all = postRepository.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(5)
    @Rollback(false)
    void testUpdate() {
        Post post = postRepository.findById(savedPostId).orElseThrow();
        String newName = "Updated Post Name";
        String newBody = "Updated post body text content";
        post.setName(newName);
        post.setBody(newBody);

        postRepository.save(post);

        Post updated = postRepository.findById(savedPostId).orElseThrow();
        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getBody()).isEqualTo(newBody);
        assertThat(updated.getSlug()).isEqualTo(savedPostSlug);
    }

    @Test
    @Order(6)
    void testFindBySlug() {
        Optional<Post> found = postRepository.findBySlug(savedPostSlug);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedPostId);
        assertThat(found.get().getSlug()).isEqualTo(savedPostSlug);
    }

    @Test
    @Order(7)
    void testFindBySlugNotFound() {
        Optional<Post> found = postRepository.findBySlug("nonexistent-slug-12345");
        assertThat(found).isEmpty();
    }

    @Test
    @Order(8)
    @Rollback(false)
    void testDelete() {
        postRepository.deleteById(savedPostId);

        Optional<Post> deleted = postRepository.findById(savedPostId);
        assertThat(deleted).isEmpty();

        userRepository.deleteById(savedUserId);
    }
}
