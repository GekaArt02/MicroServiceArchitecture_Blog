package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.CommentUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.mapper.CommentMapper;
import io.hexlet.spring.msa_blog.model.Comment;
import io.hexlet.spring.msa_blog.model.Post;
import io.hexlet.spring.msa_blog.model.User;
import io.hexlet.spring.msa_blog.repository.CommentRepository;
import io.hexlet.spring.msa_blog.util.ModelGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void testCreate() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(2L);
        CommentCreateDTO createDTO = ModelGenerator.buildCommentCreateDTO(post.getId(), author.getId());
        Comment comment = ModelGenerator.buildComment(post, author);
        Comment saved = ModelGenerator.buildComment(post, author);
        saved.setId(1L);
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setPostId(post.getId());
        commentDTO.setAuthorId(author.getId());
        commentDTO.setBody(createDTO.getBody());

        given(commentMapper.map(createDTO)).willReturn(comment);
        given(commentRepository.save(comment)).willReturn(saved);
        given(commentMapper.map(saved)).willReturn(commentDTO);

        CommentDTO result = commentService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getAuthorId()).isEqualTo(author.getId());
        assertThat(result.getBody()).isEqualTo(createDTO.getBody());
        verify(commentMapper, times(1)).map(createDTO);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testFindById() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(2L);
        Comment comment = ModelGenerator.buildComment(post, author);
        comment.setId(1L);
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setPostId(post.getId());

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(commentMapper.map(comment)).willReturn(commentDTO);

        CommentDTO result = commentService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPostId()).isEqualTo(post.getId());
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        Long fakeId = 999L;
        given(commentRepository.findById(fakeId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getById(fakeId));
        verify(commentRepository, times(1)).findById(fakeId);
        verify(commentMapper, never()).map(any(Comment.class));
    }

    @Test
    void testGetAll() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(2L);
        Comment comment = ModelGenerator.buildComment(post, author);
        comment.setId(1L);
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        List<Comment> comments = Collections.singletonList(comment);
        List<CommentDTO> dtoList = Collections.singletonList(commentDTO);

        given(commentRepository.findAll()).willReturn(comments);
        given(commentMapper.map(comment)).willReturn(commentDTO);

        List<CommentDTO> result = commentService.getAll();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(commentRepository, times(1)).findAll();
    }

    @Test
    void testGetByPostId() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(2L);
        Comment comment = ModelGenerator.buildComment(post, author);
        comment.setId(1L);
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setPostId(post.getId());
        List<Comment> comments = Collections.singletonList(comment);
        List<CommentDTO> dtoList = Collections.singletonList(commentDTO);

        given(commentRepository.findByPostId(post.getId())).willReturn(comments);
        given(commentMapper.map(comment)).willReturn(commentDTO);

        List<CommentDTO> result = commentService.getByPostId(post.getId());

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostId()).isEqualTo(post.getId());
        verify(commentRepository, times(1)).findByPostId(post.getId());
    }

    @Test
    void testUpdate() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(2L);
        Comment comment = ModelGenerator.buildComment(post, author);
        comment.setId(1L);
        CommentUpdateDTO updateDTO = new CommentUpdateDTO();
        updateDTO.setBody(JsonNullable.of("Updated comment body"));
        Comment saved = ModelGenerator.buildComment(post, author);
        saved.setId(1L);
        saved.setBody("Updated comment body");
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setBody("Updated comment body");

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(commentRepository.save(comment)).willReturn(saved);
        given(commentMapper.map(saved)).willReturn(commentDTO);

        CommentDTO result = commentService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo("Updated comment body");
        verify(commentMapper, times(1)).update(updateDTO, comment);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testUpdateNotFound() {
        Long fakeId = 999L;
        CommentUpdateDTO updateDTO = new CommentUpdateDTO();
        given(commentRepository.findById(fakeId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.update(fakeId, updateDTO));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testDelete() {
        Long id = 1L;
        given(commentRepository.existsById(id)).willReturn(true);

        commentService.delete(id);

        verify(commentRepository, times(1)).deleteById(id);
        verify(commentRepository, times(1)).existsById(id);
    }

    @Test
    void testDeleteNotFound() {
        Long fakeId = 999L;
        given(commentRepository.existsById(fakeId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> commentService.delete(fakeId));
        verify(commentRepository, never()).deleteById(any());
    }
}
