insert into article_categories(default_title)
values ('SCP Series VII')
ON CONFLICT DO NOTHING;


with
    -- categories
    SERIES_7 as (select * from article_categories where default_title = 'SCP Series VII'),
    -- langs
    ruLang as (select * from langs where lang_code = 'ru'),
    enLang as (select * from langs where lang_code = 'en')
insert
into article_categories__to__langs(article_category_id, lang_id, title, site_url)
VALUES
-- ru
((select id from SERIES_7), (select id from ruLang), 'Объекты VII', '/scp-series-7'),
-- en
((select id from SERIES_7), (select id from enLang), 'SCP Series VII', '/scp-series-7')
on conflict DO NOTHING;