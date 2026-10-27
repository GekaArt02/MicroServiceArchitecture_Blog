package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.UserCreateDTO;
import io.hexlet.spring.msa_blog.dto.UserDTO;
import io.hexlet.spring.msa_blog.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    UserDTO create(UserCreateDTO dto);

    List<UserDTO> getAll();

    UserDTO getById(Long id);

    UserDTO update(Long id, UserUpdateDTO dto);

    void delete(Long id);
}
