package io.hexlet.spring.msa_blog.controller.api;

import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostDTO;
import io.hexlet.spring.msa_blog.dto.PostUpdateDTO;
import io.hexlet.spring.msa_blog.service.CommentService;
import io.hexlet.spring.msa_blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PostDTO> index() {
        return postService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDTO create(@Valid @RequestBody PostCreateDTO dto) {
        return postService.create(dto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO show(@PathVariable Long id) {
        return postService.getById(id);
    }

    @GetMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDTO> getComments(@PathVariable Long postId) {
        return commentService.getByPostId(postId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO dto) {
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        postService.delete(id);
    }
}
