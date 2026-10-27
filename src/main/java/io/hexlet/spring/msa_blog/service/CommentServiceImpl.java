package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.CommentUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.mapper.CommentMapper;
import io.hexlet.spring.msa_blog.model.Comment;
import io.hexlet.spring.msa_blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDTO create(CommentCreateDTO dto) {
        Comment comment = commentMapper.map(dto);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.map(savedComment);
    }

    @Override
    public List<CommentDTO> getAll() {
        return commentRepository.findAll().stream()
                .map(commentMapper::map)
                .toList();
    }

    @Override
    public List<CommentDTO> getByPostId(Long postId) {
        return commentRepository.findByPostId(postId).stream()
                .map(commentMapper::map)
                .toList();
    }

    @Override
    public CommentDTO getById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
        return commentMapper.map(comment);
    }

    @Override
    @Transactional
    public CommentDTO update(Long id, CommentUpdateDTO dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
        commentMapper.update(dto, comment);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.map(savedComment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment", id);
        }
        commentRepository.deleteById(id);
    }
}
