package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.UserCreateDTO;
import io.hexlet.spring.msa_blog.dto.UserDTO;
import io.hexlet.spring.msa_blog.dto.UserUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.mapper.UserMapper;
import io.hexlet.spring.msa_blog.model.User;
import io.hexlet.spring.msa_blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDTO create(UserCreateDTO dto) {
        User user = userMapper.map(dto);
        User savedUser = userRepository.save(user);
        return userMapper.map(savedUser);
    }

    @Override
    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    @Override
    public UserDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.map(user);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userMapper.update(dto, user);
        User savedUser = userRepository.save(user);
        return userMapper.map(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}
