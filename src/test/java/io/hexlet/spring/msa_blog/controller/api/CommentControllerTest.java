package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.mapper.CommentMapper;
import io.hexlet.spring.msa_blog.model.Comment;
import io.hexlet.spring.msa_blog.model.Post;
import io.hexlet.spring.msa_blog.model.User;
import io.hexlet.spring.msa_blog.repository.CommentRepository;
import io.hexlet.spring.msa_blog.repository.PostRepository;
import io.hexlet.spring.msa_blog.repository.UserRepository;
import io.hexlet.spring.msa_blog.util.ModelGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentMapper commentMapper;

    private User testUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        testUser = ModelGenerator.buildUser();
        userRepository.save(testUser);
        testPost = ModelGenerator.buildPost(testUser);
        postRepository.save(testPost);
        testComment = ModelGenerator.buildComment(testPost, testUser);
        commentRepository.save(testComment);
    }

    @Test
    void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<CommentDTO> expected = List.of(commentMapper.map(testComment));
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(expected));
    }

    @Test
    void testCreate() throws Exception {
        CommentCreateDTO dto = ModelGenerator.buildCommentCreateDTO(testPost.getId(), testUser.getId());

        var request = post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("postId").isEqualTo(testPost.getId()),
                v -> v.node("authorId").isEqualTo(testUser.getId()),
                v -> v.node("body").isEqualTo(dto.getBody()),
                v -> v.node("id").isPresent()
        );

        List<Comment> all = commentRepository.findAll();
        assertThat(all).hasSize(2);
        Comment created = all.stream()
                .filter(c -> c.getBody().equals(dto.getBody()))
                .findFirst().orElseThrow();
        assertNotNull(created);
        assertThat(created.getPost().getId()).isEqualTo(testPost.getId());
        assertThat(created.getAuthor().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        CommentDTO expected = commentMapper.map(testComment);
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(expected));
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/comments/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate() throws Exception {
        Map<String, JsonNullable<String>> update = new HashMap<>();
        update.put("body", JsonNullable.of("Updated comment body!"));

        var request = put("/api/comments/{id}", testComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).node("body").isEqualTo("Updated comment body!");

        Comment updated = commentRepository.findById(testComment.getId()).orElseThrow();
        assertThat(updated.getBody()).isEqualTo("Updated comment body!");
    }

    @Test
    void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/comments/{id}", testComment.getId()))
                .andExpect(status().isNoContent());

        assertThat(commentRepository.existsById(testComment.getId())).isFalse();
    }

    @Test
    void testGetCommentsByPost() throws Exception {
        var result = mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
        List<CommentDTO> expected = List.of(commentMapper.map(testComment));
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(expected));
    }

    @Test
    void testGetCommentsByPostEmpty() throws Exception {
        commentRepository.deleteAll();
        var result = mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().isEmpty();
    }
}
