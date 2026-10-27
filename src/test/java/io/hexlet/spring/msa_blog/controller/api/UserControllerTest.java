package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.dto.UserCreateDTO;
import io.hexlet.spring.msa_blog.dto.UserDTO;
import io.hexlet.spring.msa_blog.mapper.UserMapper;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        testUser = ModelGenerator.buildUser();
        userRepository.save(testUser);
    }

    @Test
    void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<UserDTO> usersDTO = List.of(userMapper.map(testUser));
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(usersDTO));
    }

    @Test
    void testCreate() throws Exception {
        UserCreateDTO dto = ModelGenerator.buildUserCreateDTO();

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(dto.getFirstName()),
                v -> v.node("lastName").isEqualTo(dto.getLastName()),
                v -> v.node("email").isEqualTo(dto.getEmail()),
                v -> v.node("id").isPresent()
        );

        var user = userRepository.findByEmail(dto.getEmail()).orElseThrow();
        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(user.getLastName()).isEqualTo(dto.getLastName());
    }

    @Test
    void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        UserDTO expected = userMapper.map(testUser);
        assertThatJson(body).isEqualTo(objectMapper.writeValueAsString(expected));
    }

    @Test
    void testShowNotFound() throws Exception {
        Long fakeId = 999999L;
        mockMvc.perform(get("/api/users/{id}", fakeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate() throws Exception {
        Map<String, JsonNullable<String>> update = new HashMap<>();
        update.put("firstName", JsonNullable.of("UpdatedFirstName"));
        update.put("lastName", JsonNullable.of("UpdatedLastName"));

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo("UpdatedFirstName"),
                v -> v.node("lastName").isEqualTo("UpdatedLastName"),
                v -> v.node("email").isEqualTo(testUser.getEmail())
        );

        var updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(updatedUser.getLastName()).isEqualTo("UpdatedLastName");
        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void testUpdatePartial() throws Exception {
        Map<String, JsonNullable<String>> partial = new HashMap<>();
        partial.put("firstName", JsonNullable.of("OnlyFirstName"));

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partial));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("OnlyFirstName");
        assertThat(updatedUser.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }
}
