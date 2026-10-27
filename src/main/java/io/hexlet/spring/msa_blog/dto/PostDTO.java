package io.hexlet.spring.msa_blog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PostDTO {

    private Long id;

    private Long authorId;

    private String slug;

    private String name;

    private String body;

    private LocalDate createdAt;
}
