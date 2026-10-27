package io.hexlet.spring.msa_blog.util;

import com.github.javafaker.Faker;
import io.hexlet.spring.msa_blog.dto.CommentCreateDTO;
import io.hexlet.spring.msa_blog.dto.PostCreateDTO;
import io.hexlet.spring.msa_blog.dto.UserCreateDTO;
import io.hexlet.spring.msa_blog.model.Comment;
import io.hexlet.spring.msa_blog.model.Post;
import io.hexlet.spring.msa_blog.model.User;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;

import static org.instancio.Select.field;

@ExtendWith(InstancioExtension.class)
public class ModelGenerator {

    private static final Faker FAKER = new Faker(new Locale("en-US"));

    public static UserCreateDTO buildUserCreateDTO() {
        return Instancio.of(UserCreateDTO.class)
                .supply(field(UserCreateDTO::getFirstName), () -> FAKER.name().firstName())
                .supply(field(UserCreateDTO::getLastName), () -> FAKER.name().lastName())
                .supply(field(UserCreateDTO::getEmail), () -> FAKER.internet().emailAddress())
                .supply(field(UserCreateDTO::getPassword), () -> FAKER.internet().password(6, 20))
                .create();
    }

    public static User buildUser() {
        return Instancio.of(User.class)
                .ignore(field(User::getId))
                .ignore(field(User::getCreatedAt))
                .ignore(field(User::getUpdatedAt))
                .supply(field(User::getFirstName), () -> FAKER.name().firstName())
                .supply(field(User::getLastName), () -> FAKER.name().lastName())
                .supply(field(User::getEmail), () -> FAKER.internet().emailAddress())
                .supply(field(User::getPassword), () -> FAKER.internet().password(6, 20))
                .create();
    }

    public static PostCreateDTO buildPostCreateDTO(Long authorId) {
        String title = FAKER.book().title();
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                + "-" + FAKER.number().randomNumber(4, true);
        return Instancio.of(PostCreateDTO.class)
                .supply(field(PostCreateDTO::getAuthorId), () -> authorId)
                .supply(field(PostCreateDTO::getSlug), () -> slug)
                .supply(field(PostCreateDTO::getName), () -> title)
                .supply(field(PostCreateDTO::getBody), () -> FAKER.lorem().paragraph(5))
                .create();
    }

    public static Post buildPost(User author) {
        String title = FAKER.book().title();
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                + "-" + FAKER.number().randomNumber(4, true);
        return Instancio.of(Post.class)
                .ignore(field(Post::getId))
                .ignore(field(Post::getCreatedAt))
                .ignore(field(Post::getUpdatedAt))
                .supply(field(Post::getAuthor), () -> author)
                .supply(field(Post::getSlug), () -> slug)
                .supply(field(Post::getName), () -> title)
                .supply(field(Post::getBody), () -> FAKER.lorem().paragraph(5))
                .create();
    }

    public static CommentCreateDTO buildCommentCreateDTO(Long postId, Long authorId) {
        return Instancio.of(CommentCreateDTO.class)
                .supply(field(CommentCreateDTO::getPostId), () -> postId)
                .supply(field(CommentCreateDTO::getAuthorId), () -> authorId)
                .supply(field(CommentCreateDTO::getBody), () -> FAKER.lorem().sentence(10))
                .create();
    }

    public static Comment buildComment(Post post, User author) {
        return Instancio.of(Comment.class)
                .ignore(field(Comment::getId))
                .ignore(field(Comment::getCreatedAt))
                .ignore(field(Comment::getUpdatedAt))
                .supply(field(Comment::getPost), () -> post)
                .supply(field(Comment::getAuthor), () -> author)
                .supply(field(Comment::getBody), () -> FAKER.lorem().sentence(10))
                .create();
    }
}
