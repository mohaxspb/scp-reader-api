insert into article_categories(default_title)
values ('SCP Series RU')
ON CONFLICT DO NOTHING;


with
    -- categories
    SERIES_RU as (select * from article_categories where default_title = 'SCP Series RU'),
    SERIES_6 as (select * from article_categories where default_title = 'SCP Series VI'),
    -- langs
    ruLang as (select * from langs where lang_code = 'ru'),
    ptLang as (select * from langs where lang_code = 'pt')
insert
into article_categories__to__langs(article_category_id, lang_id, title, site_url)
VALUES
-- ru
((select id from SERIES_RU), (select id from ruLang), 'Объекты Российского филиала', '/scp-list-ru'),
-- pt
((select id from SERIES_6), (select id from ptLang), 'SCP Série 6', '/scp-series-6')
on conflict DO NOTHING;