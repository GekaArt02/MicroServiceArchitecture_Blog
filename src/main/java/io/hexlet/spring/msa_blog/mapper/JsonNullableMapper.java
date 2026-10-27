package io.hexlet.spring.msa_blog.mapper;

import org.mapstruct.Condition;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

@Component
public class JsonNullableMapper {

    public <T> JsonNullable<T> wrap(T entity) {
        return JsonNullable.of(entity);
    }

    public <T> T unwrap(JsonNullable<T> jsonNullable) {
        return jsonNullable == null ? null : jsonNullable.orElse(null);
    }

    @Condition
    @Named("isPresent")
    public <T> boolean isPresent(JsonNullable<T> nullable) {
        return nullable != null && nullable.isPresent();
    }
}
