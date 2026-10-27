-- Do not overwrite records when adopting an existing laboratory database.
INSERT INTO users (first_name, last_name, email, password, created_at, updated_at)
SELECT 'Demo', 'Author', 'demo.author@example.com', 'demo-password', CURRENT_DATE, CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo.author@example.com');

INSERT INTO posts (author_id, slug, name, body, created_at, updated_at)
SELECT id, 'welcome-to-blog', 'Welcome to the blog',
       'This post was created by the initial Flyway data migration.', CURRENT_DATE, CURRENT_DATE
FROM users WHERE email = 'demo.author@example.com'
AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'welcome-to-blog');

INSERT INTO posts (author_id, slug, name, body, created_at, updated_at)
SELECT id, 'docker-and-mysql', 'Docker and MySQL',
       'The application and its database run in separate Docker containers.', CURRENT_DATE, CURRENT_DATE
FROM users WHERE email = 'demo.author@example.com'
AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'docker-and-mysql');

INSERT INTO posts (author_id, slug, name, body, created_at, updated_at)
SELECT id, 'flyway-migrations', 'Database migrations with Flyway',
       'Versioned SQL scripts keep database structure and seed data reproducible.', CURRENT_DATE, CURRENT_DATE
FROM users WHERE email = 'demo.author@example.com'
AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'flyway-migrations');

INSERT INTO comments (post_id, author_id, body, created_at, updated_at)
SELECT p.id, u.id, 'The initial migration completed successfully.', CURRENT_DATE, CURRENT_DATE
FROM posts p JOIN users u ON u.email = 'demo.author@example.com'
WHERE p.slug = 'welcome-to-blog'
AND NOT EXISTS (SELECT 1 FROM comments c WHERE c.post_id = p.id AND c.author_id = u.id
               AND c.body = 'The initial migration completed successfully.');
