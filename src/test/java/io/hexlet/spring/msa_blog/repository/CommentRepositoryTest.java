package io.hexlet.spring.msa_blog.repository;

import io.hexlet.spring.msa_blog.model.Comment;
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

import java.util.List;
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private static Long savedUserId;
    private static Long savedPostId;
    private static Long savedCommentId;

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
    void testSavePostSecond() {
        User author = userRepository.findById(savedUserId).orElseThrow();
        Post post = ModelGenerator.buildPost(author);
        Post savedPost = postRepository.save(post);
        savedPostId = savedPost.getId();
        assertThat(savedPostId).isGreaterThan(0);
    }

    @Test
    @Order(3)
    @Rollback(false)
    void testSave() {
        Post post = postRepository.findById(savedPostId).orElseThrow();
        User author = userRepository.findById(savedUserId).orElseThrow();
        Comment comment = ModelGenerator.buildComment(post, author);

        Comment saved = commentRepository.save(comment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getBody()).isEqualTo(comment.getBody());
        assertThat(saved.getPost().getId()).isEqualTo(savedPostId);
        assertThat(saved.getAuthor().getId()).isEqualTo(savedUserId);

        savedCommentId = saved.getId();
    }

    @Test
    @Order(4)
    void testFindById() {
        Optional<Comment> found = commentRepository.findById(savedCommentId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedCommentId);
        assertThat(found.get().getBody()).isNotNull();
    }

    @Test
    @Order(5)
    void testFindAll() {
        Iterable<Comment> all = commentRepository.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(6)
    @Rollback(false)
    void testUpdate() {
        Comment comment = commentRepository.findById(savedCommentId).orElseThrow();
        String newBody = "Updated comment body for testing";
        comment.setBody(newBody);

        commentRepository.save(comment);

        Comment updated = commentRepository.findById(savedCommentId).orElseThrow();
        assertThat(updated.getBody()).isEqualTo(newBody);
    }

    @Test
    @Order(7)
    void testFindByPostId() {
        List<Comment> comments = commentRepository.findByPostId(savedPostId);

        assertThat(comments).isNotEmpty();
        assertThat(comments).allMatch(c -> c.getPost().getId().equals(savedPostId));
        assertThat(comments).anyMatch(c -> c.getId().equals(savedCommentId));
    }

    @Test
    @Order(8)
    void testFindByPostIdEmpty() {
        Long fakePostId = 999999L;
        List<Comment> comments = commentRepository.findByPostId(fakePostId);
        assertThat(comments).isEmpty();
    }

    @Test
    @Order(9)
    @Rollback(false)
    void testDelete() {
        commentRepository.deleteById(savedCommentId);

        Optional<Comment> deleted = commentRepository.findById(savedCommentId);
        assertThat(deleted).isEmpty();

        postRepository.deleteById(savedPostId);
        userRepository.deleteById(savedUserId);
    }
}
