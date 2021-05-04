insert into server_settings (key, value, author_id, created, updated)
VALUES ('DAILY_CATEGORIES_SYNC_TASK_ENABLED', true, 1, now(), now()),
       ('DAILY_RATED_SYNC_TASK_ENABLED', true, 1, now(), now())
ON CONFLICT DO NOTHING;