CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATE,
    updated_at DATE,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    slug VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at DATE,
    updated_at DATE,
    CONSTRAINT uk_posts_slug UNIQUE (slug),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    created_at DATE,
    updated_at DATE,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);
