package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.config.JacksonConfig;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostDTO;
import io.hexlet.spring.msa_blog.dto.PostUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.service.CommentService;
import io.hexlet.spring.msa_blog.service.PostService;
import io.hexlet.spring.msa_blog.util.ModelGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PostController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Import(JacksonConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class PostControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    private PostDTO postDTO;
    private PostCreateDTO createDTO;
    private PostUpdateDTO updateDTO;
    private CommentDTO commentDTO;

    @BeforeEach
    void setUp() {
        postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setAuthorId(1L);
        postDTO.setSlug("spring-boot-guide");
        postDTO.setName("Spring Boot Guide");
        postDTO.setBody("Full text content of the post");
        postDTO.setCreatedAt(LocalDate.now());

        createDTO = ModelGenerator.buildPostCreateDTO(1L);

        updateDTO = new PostUpdateDTO();
        updateDTO.setAuthorId(JsonNullable.of(1L));
        updateDTO.setSlug(JsonNullable.of("updated-slug"));
        updateDTO.setName(JsonNullable.of("Updated Title"));
        updateDTO.setBody(JsonNullable.of("Updated body"));

        commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setPostId(1L);
        commentDTO.setAuthorId(1L);
        commentDTO.setBody("Great post!");
        commentDTO.setCreatedAt(LocalDate.now());
    }

    @Test
    @Order(1)
    void testSave() throws Exception {
        given(postService.create(any(PostCreateDTO.class))).willReturn(postDTO);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.authorId", is(1)))
                .andExpect(jsonPath("$.slug", is("spring-boot-guide")))
                .andExpect(jsonPath("$.name", is("Spring Boot Guide")));
    }

    @Test
    @Order(2)
    void testGetById() throws Exception {
        given(postService.getById(1L)).willReturn(postDTO);

        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.slug", is("spring-boot-guide")))
                .andExpect(jsonPath("$.name", is("Spring Boot Guide")));
    }

    @Test
    @Order(3)
    void testGetByIdNotFound() throws Exception {
        given(postService.getById(999L))
                .willThrow(new ResourceNotFoundException("Post", 999L));

        mockMvc.perform(get("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void testGetAll() throws Exception {
        List<PostDTO> posts = Collections.singletonList(postDTO);
        given(postService.getAll()).willReturn(posts);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].slug", is("spring-boot-guide")));
    }

    @Test
    @Order(5)
    void testUpdate() throws Exception {
        PostDTO updated = new PostDTO();
        updated.setId(1L);
        updated.setAuthorId(1L);
        updated.setSlug("updated-slug");
        updated.setName("Updated Title");
        updated.setBody("Updated body");
        updated.setCreatedAt(postDTO.getCreatedAt());

        given(postService.update(eq(1L), any(PostUpdateDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", is("updated-slug")))
                .andExpect(jsonPath("$.name", is("Updated Title")))
                .andExpect(jsonPath("$.body", is("Updated body")));
    }

    @Test
    @Order(6)
    void testDelete() throws Exception {
        doNothing().when(postService).delete(1L);

        mockMvc.perform(delete("/api/posts/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    void testGetCommentsByPost() throws Exception {
        List<CommentDTO> comments = Collections.singletonList(commentDTO);
        given(commentService.getByPostId(1L)).willReturn(comments);

        mockMvc.perform(get("/api/posts/{postId}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].postId", is(1)))
                .andExpect(jsonPath("$[0].body", is("Great post!")));
    }

    @Test
    @Order(8)
    void testDeleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Post", 999L))
                .when(postService).delete(999L);

        mockMvc.perform(delete("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
