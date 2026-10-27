package io.hexlet.spring.msa_blog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CommentDTO {

    private Long id;

    private Long postId;

    private Long authorId;

    private String body;

    private LocalDate createdAt;
}
