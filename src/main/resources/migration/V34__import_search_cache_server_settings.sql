insert into server_settings (key, value, author_id, created, updated)
VALUES ('SEARCH_RESULTS_CACHE_SIZE', 100, 1, now(), now()),
       ('SEARCH_RESULTS_PAGES_COUNT', 10, 1, now(), now()),
       ('SEARCH_RESULTS_PAGE_SIZE', 20, 1, now(), now())
ON CONFLICT DO NOTHING;