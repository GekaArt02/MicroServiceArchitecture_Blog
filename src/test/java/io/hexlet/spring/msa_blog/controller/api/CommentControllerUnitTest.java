package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.config.JacksonConfig;
import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.CommentUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.service.CommentService;
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
        controllers = CommentController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Import(JacksonConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class CommentControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private CommentDTO commentDTO;
    private CommentCreateDTO createDTO;
    private CommentUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setPostId(1L);
        commentDTO.setAuthorId(1L);
        commentDTO.setBody("This is a test comment");
        commentDTO.setCreatedAt(LocalDate.now());

        createDTO = ModelGenerator.buildCommentCreateDTO(1L, 1L);

        updateDTO = new CommentUpdateDTO();
        updateDTO.setBody(JsonNullable.of("Updated comment text"));
    }

    @Test
    @Order(1)
    void testSave() throws Exception {
        given(commentService.create(any(CommentCreateDTO.class))).willReturn(commentDTO);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.postId", is(1)))
                .andExpect(jsonPath("$.authorId", is(1)))
                .andExpect(jsonPath("$.body", is("This is a test comment")));
    }

    @Test
    @Order(2)
    void testGetById() throws Exception {
        given(commentService.getById(1L)).willReturn(commentDTO);

        mockMvc.perform(get("/api/comments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.body", is("This is a test comment")))
                .andExpect(jsonPath("$.postId", is(1)));
    }

    @Test
    @Order(3)
    void testGetByIdNotFound() throws Exception {
        given(commentService.getById(999L))
                .willThrow(new ResourceNotFoundException("Comment", 999L));

        mockMvc.perform(get("/api/comments/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void testGetAll() throws Exception {
        List<CommentDTO> comments = Collections.singletonList(commentDTO);
        given(commentService.getAll()).willReturn(comments);

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].body", is("This is a test comment")));
    }

    @Test
    @Order(5)
    void testUpdate() throws Exception {
        CommentDTO updated = new CommentDTO();
        updated.setId(1L);
        updated.setPostId(1L);
        updated.setAuthorId(1L);
        updated.setBody("Updated comment text");
        updated.setCreatedAt(commentDTO.getCreatedAt());

        given(commentService.update(eq(1L), any(CommentUpdateDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/comments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body", is("Updated comment text")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @Order(6)
    void testDelete() throws Exception {
        doNothing().when(commentService).delete(1L);

        mockMvc.perform(delete("/api/comments/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    void testDeleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Comment", 999L))
                .when(commentService).delete(999L);

        mockMvc.perform(delete("/api/comments/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
