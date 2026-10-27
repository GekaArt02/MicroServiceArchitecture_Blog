package io.hexlet.spring.msa_blog.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.spring.msa_blog.config.JacksonConfig;
import io.hexlet.spring.msa_blog.dto.UserCreateDTO;
import io.hexlet.spring.msa_blog.dto.UserDTO;
import io.hexlet.spring.msa_blog.dto.UserUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.service.UserService;
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
        controllers = UserController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Import(JacksonConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("Alice");
        userDTO.setLastName("Smith");
        userDTO.setEmail("alice@example.com");
        userDTO.setCreatedAt(LocalDate.now());

        createDTO = ModelGenerator.buildUserCreateDTO();

        updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("Bob"));
        updateDTO.setLastName(JsonNullable.of("Johnson"));
        updateDTO.setEmail(JsonNullable.of("bob@example.com"));
        updateDTO.setPassword(JsonNullable.of("newpass"));
    }

    @Test
    @Order(1)
    void testSave() throws Exception {
        given(userService.create(any(UserCreateDTO.class))).willReturn(userDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    @Order(2)
    void testGetById() throws Exception {
        given(userService.getById(1L)).willReturn(userDTO);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    @Order(3)
    void testGetByIdNotFound() throws Exception {
        given(userService.getById(999L))
                .willThrow(new ResourceNotFoundException("User", 999L));

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void testGetAll() throws Exception {
        List<UserDTO> users = Collections.singletonList(userDTO);
        given(userService.getAll()).willReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstName", is("Alice")));
    }

    @Test
    @Order(5)
    void testUpdate() throws Exception {
        UserDTO updated = new UserDTO();
        updated.setId(1L);
        updated.setFirstName("Bob");
        updated.setLastName("Johnson");
        updated.setEmail("bob@example.com");
        updated.setCreatedAt(userDTO.getCreatedAt());

        given(userService.update(eq(1L), any(UserUpdateDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Bob")))
                .andExpect(jsonPath("$.lastName", is("Johnson")))
                .andExpect(jsonPath("$.email", is("bob@example.com")));
    }

    @Test
    @Order(6)
    void testDelete() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    void testDeleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User", 999L))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
