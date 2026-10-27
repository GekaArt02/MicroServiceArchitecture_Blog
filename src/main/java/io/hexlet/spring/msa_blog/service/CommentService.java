package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.CommentUpdateDTO;

import java.util.List;

public interface CommentService {

    CommentDTO create(CommentCreateDTO dto);

    List<CommentDTO> getAll();

    List<CommentDTO> getByPostId(Long postId);

    CommentDTO getById(Long id);

    CommentDTO update(Long id, CommentUpdateDTO dto);

    void delete(Long id);
}
