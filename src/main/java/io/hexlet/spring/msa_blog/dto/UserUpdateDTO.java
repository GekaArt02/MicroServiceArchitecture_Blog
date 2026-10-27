package io.hexlet.spring.msa_blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserUpdateDTO {

    private JsonNullable<@NotBlank String> firstName;

    private JsonNullable<@NotBlank String> lastName;

    private JsonNullable<@NotBlank String> email;

    private JsonNullable<@NotBlank String> password;
}
