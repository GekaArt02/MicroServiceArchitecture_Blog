package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostDTO;
import io.hexlet.spring.msa_blog.dto.PostUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.mapper.PostMapper;
import io.hexlet.spring.msa_blog.model.Post;
import io.hexlet.spring.msa_blog.model.User;
import io.hexlet.spring.msa_blog.repository.PostRepository;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void testCreate() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        PostCreateDTO createDTO = ModelGenerator.buildPostCreateDTO(author.getId());
        Post post = ModelGenerator.buildPost(author);
        Post saved = ModelGenerator.buildPost(author);
        saved.setId(1L);
        saved.setName(createDTO.getName());
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setAuthorId(author.getId());
        postDTO.setName(createDTO.getName());

        given(postMapper.map(createDTO)).willReturn(post);
        given(postRepository.save(post)).willReturn(saved);
        given(postMapper.map(saved)).willReturn(postDTO);

        PostDTO result = postService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAuthorId()).isEqualTo(author.getId());
        assertThat(result.getName()).isEqualTo(createDTO.getName());
        verify(postMapper, times(1)).map(createDTO);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testFindById() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(1L);
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setSlug(post.getSlug());
        postDTO.setAuthorId(author.getId());

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postMapper.map(post)).willReturn(postDTO);

        PostDTO result = postService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSlug()).isEqualTo(post.getSlug());
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        Long fakeId = 999L;
        given(postRepository.findById(fakeId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getById(fakeId));
        verify(postRepository, times(1)).findById(fakeId);
        verify(postMapper, never()).map(any(Post.class));
    }

    @Test
    void testGetAll() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(1L);
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);
        List<Post> posts = Collections.singletonList(post);
        List<PostDTO> dtoList = Collections.singletonList(postDTO);

        given(postRepository.findAll()).willReturn(posts);
        given(postMapper.map(post)).willReturn(postDTO);

        List<PostDTO> result = postService.getAll();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(postRepository, times(1)).findAll();
    }

    @Test
    void testUpdate() {
        User author = ModelGenerator.buildUser();
        author.setId(1L);
        Post post = ModelGenerator.buildPost(author);
        post.setId(1L);
        PostUpdateDTO updateDTO = new PostUpdateDTO();
        updateDTO.setName(JsonNullable.of("New Title"));
        Post saved = ModelGenerator.buildPost(author);
        saved.setId(1L);
        saved.setName("New Title");
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setName("New Title");

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postRepository.save(post)).willReturn(saved);
        given(postMapper.map(saved)).willReturn(postDTO);

        PostDTO result = postService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Title");
        verify(postMapper, times(1)).update(updateDTO, post);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testUpdateNotFound() {
        Long fakeId = 999L;
        PostUpdateDTO updateDTO = new PostUpdateDTO();
        given(postRepository.findById(fakeId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.update(fakeId, updateDTO));
        verify(postRepository, never()).save(any());
    }

    @Test
    void testDelete() {
        Long id = 1L;
        given(postRepository.existsById(id)).willReturn(true);

        postService.delete(id);

        verify(postRepository, times(1)).deleteById(id);
        verify(postRepository, times(1)).existsById(id);
    }

    @Test
    void testDeleteNotFound() {
        Long fakeId = 999L;
        given(postRepository.existsById(fakeId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.delete(fakeId));
        verify(postRepository, never()).deleteById(any());
    }
}
