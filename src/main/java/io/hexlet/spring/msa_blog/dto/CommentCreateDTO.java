package io.hexlet.spring.msa_blog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateDTO {

    @NotNull
    private Long postId;

    @NotNull
    private Long authorId;

    @NotBlank
    private String body;
}
