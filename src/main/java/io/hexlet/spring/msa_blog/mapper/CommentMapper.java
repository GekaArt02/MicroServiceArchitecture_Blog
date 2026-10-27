package io.hexlet.spring.msa_blog.mapper;

import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.CommentDTO;
import io.hexlet.spring.msa_blog.dto.CommentUpdateDTO;
import io.hexlet.spring.msa_blog.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class CommentMapper {

    @Mapping(target = "post", source = "postId")
    @Mapping(target = "author", source = "authorId")
    public abstract Comment map(CommentCreateDTO dto);

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "author.id", target = "authorId")
    public abstract CommentDTO map(Comment model);

    public abstract void update(CommentUpdateDTO dto, @MappingTarget Comment model);
}
