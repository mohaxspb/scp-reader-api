insert into server_settings (key, value, author_id, created, updated)
VALUES ('MOST_RECENT_ARTICLES_CACHE_SIZE', 300, 1, now(), now()),
       ('MOST_RATED_ARTICLES_CACHE_SIZE', 300, 1, now(), now())
ON CONFLICT DO NOTHING;