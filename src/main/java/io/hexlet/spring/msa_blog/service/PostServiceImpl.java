package io.hexlet.spring.msa_blog.service;

import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostDTO;
import io.hexlet.spring.msa_blog.dto.PostUpdateDTO;
import io.hexlet.spring.msa_blog.exception.ResourceNotFoundException;
import io.hexlet.spring.msa_blog.mapper.PostMapper;
import io.hexlet.spring.msa_blog.model.Post;
import io.hexlet.spring.msa_blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public PostDTO create(PostCreateDTO dto) {
        Post post = postMapper.map(dto);
        Post savedPost = postRepository.save(post);
        return postMapper.map(savedPost);
    }

    @Override
    public List<PostDTO> getAll() {
        return postRepository.findAll().stream()
                .map(postMapper::map)
                .toList();
    }

    @Override
    public PostDTO getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        return postMapper.map(post);
    }

    @Override
    @Transactional
    public PostDTO update(Long id, PostUpdateDTO dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        postMapper.update(dto, post);
        Post savedPost = postRepository.save(post);
        return postMapper.map(savedPost);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post", id);
        }
        postRepository.deleteById(id);
    }
}
