package io.hexlet.spring.msa_blog.validation;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;
import org.openapitools.jackson.nullable.JsonNullable;

/** Validate supplied PATCH values while leaving omitted fields unchanged. */
public class JsonNullableValueExtractor implements ValueExtractor<JsonNullable<@ExtractedValue ?>> {
    @Override
    public void extractValues(JsonNullable<?> originalValue, ValueReceiver receiver) {
        if (originalValue.isPresent()) {
            receiver.value(null, originalValue.get());
        }
    }
}
