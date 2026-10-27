package io.hexlet.spring.msa_blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class CommentUpdateDTO {

    private JsonNullable<@NotBlank String> body;
}
