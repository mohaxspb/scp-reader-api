create table if not exists article_categories
(
    id            BIGSERIAL not null,
    default_title text      not null,
    created       timestamp,
    updated       timestamp,
    PRIMARY KEY (id)
);

alter table article_categories
    drop constraint if exists default_title_unique;
alter table article_categories
    add constraint default_title_unique unique (default_title);

insert into article_categories(default_title)
values ('SCP Series I'),
       ('SCP Series II'),
       ('SCP Series III'),
       ('SCP Series IV'),
       ('SCP Series V')
ON CONFLICT DO NOTHING;

create table if not exists article_categories__to__langs
(
    id                  BIGSERIAL not null,
    article_category_id bigint    not null,
    lang_id             varchar   not null,
    title               text      not null,
    site_url            text default null,
    created             timestamp,
    updated             timestamp,
    PRIMARY KEY (id)
);

ALTER TABLE article_categories__to__langs
    drop constraint IF EXISTS fk_article_category_id__to__article_categories CASCADE;
alter table article_categories__to__langs
    add constraint fk_article_category_id__to__article_categories
        foreign key (article_category_id)
            REFERENCES article_categories (id);

ALTER TABLE article_categories__to__langs
    drop constraint IF EXISTS fk_lang_id__to__langs CASCADE;
alter table article_categories__to__langs
    add constraint fk_lang_id__to__langs
        foreign key (lang_id)
            REFERENCES langs (id);

alter table article_categories__to__langs
    drop constraint if exists category_to_lang_unique;
alter table article_categories__to__langs
    add constraint category_to_lang_unique unique (article_category_id, lang_id);

with
    -- categories
    SERIES_1 as (select * from article_categories where default_title = 'SCP Series I'),
    SERIES_2 as (select * from article_categories where default_title = 'SCP Series II'),
    SERIES_3 as (select * from article_categories where default_title = 'SCP Series III'),
    SERIES_4 as (select * from article_categories where default_title = 'SCP Series IV'),
    SERIES_5 as (select * from article_categories where default_title = 'SCP Series V'),
    -- langs
    ruLang as (select * from langs where lang_code = 'ru'),
    enLang as (select * from langs where lang_code = 'en'),
    plLang as (select * from langs where lang_code = 'pl'),
    deLang as (select * from langs where lang_code = 'de'),
    frLang as (select * from langs where lang_code = 'fr'),
    itLang as (select * from langs where lang_code = 'it'),
    esLang as (select * from langs where lang_code = 'es'),
    ptLang as (select * from langs where lang_code = 'pt'),
    zhLang as (select * from langs where lang_code = 'zh')
insert
into article_categories__to__langs(article_category_id, lang_id, title, site_url)
VALUES
-- ru
((select id from SERIES_1), (select id from ruLang), 'Объекты I', '/scp-list'),
((select id from SERIES_2), (select id from ruLang), 'Объекты II', '/scp-list-2'),
((select id from SERIES_3), (select id from ruLang), 'Объекты III', '/scp-list-3'),
((select id from SERIES_4), (select id from ruLang), 'Объекты IV', '/scp-list-4'),
((select id from SERIES_5), (select id from ruLang), 'Объекты V', '/scp-list-5'),
-- en
((select id from SERIES_1), (select id from enLang), (select default_title from SERIES_1), '/scp-series'),
((select id from SERIES_2), (select id from enLang), (select default_title from SERIES_2), '/scp-series-2'),
((select id from SERIES_3), (select id from enLang), (select default_title from SERIES_3), '/scp-series-3'),
((select id from SERIES_4), (select id from enLang), (select default_title from SERIES_4), '/scp-series-4'),
((select id from SERIES_5), (select id from enLang), (select default_title from SERIES_5), '/scp-series-5'),
-- pl
((select id from SERIES_1), (select id from plLang), 'Lista angielska 1', '/lista-eng'),
((select id from SERIES_2), (select id from plLang), 'Lista angielska 2', '/lista-eng-2'),
((select id from SERIES_3), (select id from plLang), 'Lista angielska 3', '/lista-eng-3'),
((select id from SERIES_4), (select id from plLang), 'Lista angielska 4', '/lista-eng-4'),
((select id from SERIES_5), (select id from plLang), 'Lista angielska 5', '/lista-eng-5'),
-- de
((select id from SERIES_1), (select id from deLang), 'SCP Serie 1', '/scp-series'),
((select id from SERIES_2), (select id from deLang), 'SCP Serie 2', '/scp-series-2'),
((select id from SERIES_3), (select id from deLang), 'SCP Serie 3', '/scp-series-3'),
((select id from SERIES_4), (select id from deLang), 'SCP Serie 4', '/scp-series-4'),
((select id from SERIES_5), (select id from deLang), 'SCP Serie 5', '/scp-series-5'),
-- fr
((select id from SERIES_1), (select id from frLang), 'Liste de 001 à 999', '/scp-series'),
((select id from SERIES_2), (select id from frLang), 'Liste de 1000 à 1999', '/scp-series-2'),
((select id from SERIES_3), (select id from frLang), 'Liste de 2000 à 2999', '/scp-series-3'),
((select id from SERIES_4), (select id from frLang), 'Liste de 3000 à 3999', '/scp-series-4'),
((select id from SERIES_5), (select id from frLang), 'Liste de 4000 à 4999', '/scp-series-5'),
-- it
((select id from SERIES_1), (select id from itLang), 'Serie SCP I', '/scp-series'),
((select id from SERIES_2), (select id from itLang), 'Serie SCP II', '/scp-series-2'),
((select id from SERIES_3), (select id from itLang), 'Serie SCP III', '/scp-series-3'),
((select id from SERIES_4), (select id from itLang), 'Serie SCP IV', '/scp-series-4'),
((select id from SERIES_5), (select id from itLang), 'Serie SCP V', '/scp-series-5'),
-- es
((select id from SERIES_1), (select id from esLang), 'Serie SCP I', '/scp-series'),
((select id from SERIES_2), (select id from esLang), 'Serie SCP II', '/scp-series-2'),
((select id from SERIES_3), (select id from esLang), 'Serie SCP III', '/scp-series-3'),
((select id from SERIES_4), (select id from esLang), 'Serie SCP IV', '/scp-series-4'),
((select id from SERIES_5), (select id from esLang), 'Serie SCP V', '/scp-series-5'),
-- pt
((select id from SERIES_1), (select id from ptLang), 'SCP Série 1', '/scp-series'),
((select id from SERIES_2), (select id from ptLang), 'SCP Série 2', '/scp-series-2'),
((select id from SERIES_3), (select id from ptLang), 'SCP Série 3', '/scp-series-3'),
((select id from SERIES_4), (select id from ptLang), 'SCP Série 4', '/scp-series-4'),
((select id from SERIES_5), (select id from ptLang), 'SCP Série 5', '/scp-series-5'),
-- zh
((select id from SERIES_1), (select id from zhLang), 'SCP系列 1', '/scp-series'),
((select id from SERIES_2), (select id from zhLang), 'SCP系列 2', '/scp-series-2'),
((select id from SERIES_3), (select id from zhLang), 'SCP系列 3', '/scp-series-3'),
((select id from SERIES_4), (select id from zhLang), 'SCP系列 4', '/scp-series-4'),
((select id from SERIES_5), (select id from zhLang), 'SCP系列 5', '/scp-series-5')
ON CONFLICT DO NOTHING;


create table if not exists article_categories_to_lang__to__articles_to_lang
(
    id                          BIGSERIAL not null,
    article_category_to_lang_id bigint    not null,
    article_to_lang_id          bigint    not null,
    order_in_category           smallint,
    created                     timestamp,
    updated                     timestamp,
    PRIMARY KEY (id)
);

-- add fk constraints
ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    drop constraint IF EXISTS fk_article_to_lang_id__to__articles_langs CASCADE;
alter table article_categories_to_lang__to__articles_to_lang
    add constraint fk_article_to_lang_id__to__articles_langs
        foreign key (article_to_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    drop constraint IF EXISTS fk_article_category_to_lang_id__to__article_categories_to_langs CASCADE;
alter table article_categories_to_lang__to__articles_to_lang
    add constraint fk_article_category_to_lang_id__to__article_categories_to_langs
        foreign key (article_category_to_lang_id)
            REFERENCES article_categories__to__langs (id);

-- add unique constraint
alter table if exists article_categories_to_lang__to__articles_to_lang
    drop constraint if exists article_category_order_unique;
alter table article_categories_to_lang__to__articles_to_lang
    add constraint article_category_order_unique
        unique (article_category_to_lang_id, article_to_lang_id, order_in_category);