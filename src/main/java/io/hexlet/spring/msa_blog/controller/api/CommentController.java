package io.hexlet.spring.msa_blog.controller.api;

import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.CommentUpdateDTO;
import io.hexlet.spring.msa_blog.service.CommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDTO> index() {
        return commentService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDTO create(@Valid @RequestBody CommentCreateDTO dto) {
        return commentService.create(dto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDTO show(@PathVariable Long id) {
        return commentService.getById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDTO update(@PathVariable Long id, @Valid @RequestBody CommentUpdateDTO dto) {
        return commentService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        commentService.delete(id);
    }
}
