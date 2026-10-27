package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostDTO;
import io.hexlet.spring.msa_blog.dto.PostUpdateDTO;

import java.util.List;

public interface PostService {

    PostDTO create(PostCreateDTO dto);

    List<PostDTO> getAll();

    PostDTO getById(Long id);

    PostDTO update(Long id, PostUpdateDTO dto);

    void delete(Long id);
}
