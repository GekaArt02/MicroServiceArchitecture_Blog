package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.repository.CommentRepository;
import io.hexlet.spring.msa_blog.repository.PostRepository;
import io.hexlet.spring.msa_blog.repository.UserRepository;
import io.hexlet.spring.msa_blog.util.ModelGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiValidationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository users;
    @Autowired private PostRepository posts;
    @Autowired private CommentRepository comments;
    private Long userId;
    private Long postId;
    private Long commentId;

    @BeforeEach
    void setUp() {
        comments.deleteAll();
        posts.deleteAll();
        users.deleteAll();
        var user = users.save(ModelGenerator.buildUser());
        var post = posts.save(ModelGenerator.buildPost(user));
        var comment = comments.save(ModelGenerator.buildComment(post, user));
        userId = user.getId();
        postId = post.getId();
        commentId = comment.getId();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"name\":null}", "{\"name\":\"   \"}",
            "{\"body\":\"\"}", "{\"slug\":null}", "{\"authorId\":null}"})
    void invalidPostUpdateIsRejectedWithoutChangingData(String json) throws Exception {
        var original = posts.findById(postId).orElseThrow().getName();
        mvc.perform(put("/api/posts/{id}", postId).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        assertThat(posts.findById(postId).orElseThrow().getName()).isEqualTo(original);
    }

    @Test
    void omittedUpdateFieldsArePreserved() throws Exception {
        var original = posts.findById(postId).orElseThrow();
        mvc.perform(put("/api/posts/{id}", postId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Changed\"}"))
                .andExpect(status().isOk());
        var updated = posts.findById(postId).orElseThrow();
        assertThat(updated.getBody()).isEqualTo(original.getBody());
        assertThat(updated.getSlug()).isEqualTo(original.getSlug());
        assertThat(updated.getAuthor().getId()).isEqualTo(userId);
    }

    @Test
    void blankCreateAndNullUserAndCommentUpdatesAreRejected() throws Exception {
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("authorId", userId, "slug", "blank-test", "name", " ", "body", "text"))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":null}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/comments/{id}", commentId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"body\":null}")).andExpect(status().isBadRequest());
    }

    @Test
    void missingReferencesReturn404() throws Exception {
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("authorId", Long.MAX_VALUE, "slug", "missing", "name", "title", "body", "text"))))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/posts/{id}", postId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"authorId\":9223372036854775807}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("postId", Long.MAX_VALUE, "authorId", userId, "body", "text"))))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("postId", postId, "authorId", Long.MAX_VALUE, "body", "text"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateEmailAndSlugReturn409() throws Exception {
        var user = users.findById(userId).orElseThrow();
        var post = posts.findById(postId).orElseThrow();
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("firstName", "Test", "lastName", "User", "email", user.getEmail(), "password", "test"))))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(
                Map.of("authorId", userId, "slug", post.getSlug(), "name", "title", "body", "text"))))
                .andExpect(status().isConflict());
    }

    @Test
    void linkedRecordsCannotBeDeletedButOrderedDeletionWorks() throws Exception {
        mvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isConflict());
        mvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isConflict());
        assertThat(posts.existsById(postId)).isTrue();
        assertThat(users.existsById(userId)).isTrue();
        mvc.perform(delete("/api/comments/{id}", commentId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNoContent());
    }
}
