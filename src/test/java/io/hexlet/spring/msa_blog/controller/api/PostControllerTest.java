package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostDTO;
import io.hexlet.spring.msa_blog.mapper.PostMapper;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostMapper postMapper;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        testUser = ModelGenerator.buildUser();
        userRepository.save(testUser);
        testPost = ModelGenerator.buildPost(testUser);
        postRepository.save(testPost);
    }

    @Test
    void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<PostDTO> postsDTO = List.of(postMapper.map(testPost));
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(postsDTO));
    }

    @Test
    void testCreate() throws Exception {
        PostCreateDTO dto = ModelGenerator.buildPostCreateDTO(testUser.getId());

        var request = post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("authorId").isEqualTo(testUser.getId()),
                v -> v.node("slug").isEqualTo(dto.getSlug()),
                v -> v.node("name").isEqualTo(dto.getName()),
                v -> v.node("body").isEqualTo(dto.getBody()),
                v -> v.node("id").isPresent()
        );

        Post post = postRepository.findBySlug(dto.getSlug()).orElseThrow();
        assertNotNull(post);
        assertThat(post.getName()).isEqualTo(dto.getName());
        assertThat(post.getAuthor().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/posts/{id}", testPost.getId()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        PostDTO expected = postMapper.map(testPost);
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(expected));
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate() throws Exception {
        Map<String, JsonNullable<?>> update = new HashMap<>();
        update.put("name", JsonNullable.of("Updated Post Name"));
        update.put("body", JsonNullable.of("Updated content"));

        var request = put("/api/posts/{id}", testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo("Updated Post Name"),
                v -> v.node("body").isEqualTo("Updated content"),
                v -> v.node("slug").isEqualTo(testPost.getSlug())
        );

        Post updated = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Post Name");
        assertThat(updated.getBody()).isEqualTo("Updated content");
        assertThat(updated.getSlug()).isEqualTo(testPost.getSlug());
    }

    @Test
    void testUpdatePartial() throws Exception {
        Map<String, JsonNullable<?>> partial = new HashMap<>();
        partial.put("slug", JsonNullable.of("new-slug-only"));

        var request = put("/api/posts/{id}", testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partial));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        Post updated = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updated.getSlug()).isEqualTo("new-slug-only");
        assertThat(updated.getName()).isEqualTo(testPost.getName());
    }

    @Test
    void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", testPost.getId()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(testPost.getId())).isFalse();
    }
}
