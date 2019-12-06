create table if not exists article_categories
(
    id            BIGSERIAL not null,
    default_title text      not null,
    created       timestamp,
    updated       timestamp,
    PRIMARY KEY (id)
);

insert into article_categories(default_title)
values ('SCP Series I'),
       ('SCP Series II'),
       ('SCP Series III'),
       ('SCP Series IV'),
       ('SCP Series V');

create table if not exists article_category_titles_for_langs
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

ALTER TABLE article_category_titles_for_langs
    drop constraint IF EXISTS fk_article_category_id__to__article_categories CASCADE;
alter table article_category_titles_for_langs
    add constraint fk_article_category_id__to__article_categories
        foreign key (article_category_id)
            REFERENCES article_categories (id);

ALTER TABLE article_category_titles_for_langs
    drop constraint IF EXISTS fk_lang_id__to__langs CASCADE;
alter table article_category_titles_for_langs
    add constraint fk_lang_id__to__langs
        foreign key (lang_id)
            REFERENCES langs (id);

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
    cnLang as (select * from langs where lang_code = 'cn')
insert
into article_category_titles_for_langs(article_category_id, lang_id, title, site_url)
VALUES
-- ru
(SERIES_1.id, ruLang.id, 'Объекты I', '/scp-list'),
(SERIES_2.id, ruLang.id, 'Объекты II', '/scp-list-2'),
(SERIES_3.id, ruLang.id, 'Объекты III', '/scp-list-3'),
(SERIES_4.id, ruLang.id, 'Объекты IV', '/scp-list-4'),
(SERIES_5.id, ruLang.id, 'Объекты V', '/scp-list-5'),
-- en
(SERIES_1.id, enLang.id, SERIES_1.default_title, '/scp-series'),
(SERIES_2.id, enLang.id, SERIES_2.default_title, '/scp-series-2'),
(SERIES_3.id, enLang.id, SERIES_3.default_title, '/scp-series-3'),
(SERIES_4.id, enLang.id, SERIES_4.default_title, '/scp-series-4'),
(SERIES_5.id, enLang.id, SERIES_5.default_title, '/scp-series-5'),
-- pl
(SERIES_1.id, plLang.id, 'Lista angielska 1', '/lista-eng'),
(SERIES_2.id, plLang.id, 'Lista angielska 2', '/lista-eng-2'),
(SERIES_3.id, plLang.id, 'Lista angielska 3', '/lista-eng-3'),
(SERIES_4.id, plLang.id, 'Lista angielska 4', '/lista-eng-4'),
(SERIES_5.id, plLang.id, 'Lista angielska 5', '/lista-eng-5'),
-- de
(SERIES_1.id, deLang.id, 'SCP Serie 1', '/scp-series'),
(SERIES_2.id, deLang.id, 'SCP Serie 2', '/scp-series-2'),
(SERIES_3.id, deLang.id, 'SCP Serie 3', '/scp-series-3'),
(SERIES_4.id, deLang.id, 'SCP Serie 4', '/scp-series-4'),
(SERIES_5.id, deLang.id, 'SCP Serie 5', '/scp-series-5'),
-- fr
(SERIES_1.id, frLang.id, 'Liste de 001 à 999', '/scp-series'),
(SERIES_2.id, frLang.id, 'Liste de 1000 à 1999', '/scp-series-2'),
(SERIES_3.id, frLang.id, 'Liste de 2000 à 2999', '/scp-series-3'),
(SERIES_4.id, frLang.id, 'Liste de 3000 à 3999', '/scp-series-4'),
(SERIES_5.id, frLang.id, 'Liste de 4000 à 4999', '/scp-series-5'),
-- it
(SERIES_1.id, itLang.id, 'Serie SCP I', '/scp-series'),
(SERIES_2.id, itLang.id, 'Serie SCP II', '/scp-series-2'),
(SERIES_3.id, itLang.id, 'Serie SCP III', '/scp-series-3'),
(SERIES_4.id, itLang.id, 'Serie SCP IV', '/scp-series-4'),
(SERIES_5.id, itLang.id, 'Serie SCP V', '/scp-series-5'),
-- es
(SERIES_1.id, esLang.id, 'Serie SCP I', '/scp-series'),
(SERIES_2.id, esLang.id, 'Serie SCP II', '/scp-series-2'),
(SERIES_3.id, esLang.id, 'Serie SCP III', '/scp-series-3'),
(SERIES_4.id, esLang.id, 'Serie SCP IV', '/scp-series-4'),
(SERIES_5.id, esLang.id, 'Serie SCP V', '/scp-series-5'),
-- pt
(SERIES_1.id, ptLang.id, 'SCP Série 1', '/scp-series'),
(SERIES_2.id, ptLang.id, 'SCP Série 2', '/scp-series-2'),
(SERIES_3.id, ptLang.id, 'SCP Série 3', '/scp-series-3'),
(SERIES_4.id, ptLang.id, 'SCP Série 4', '/scp-series-4'),
(SERIES_5.id, ptLang.id, 'SCP Série 5', '/scp-series-5'),
-- cn
(SERIES_1.id, cnLang.id, 'SCP系列 1', '/scp-series'),
(SERIES_2.id, cnLang.id, 'SCP系列 2', '/scp-series-2'),
(SERIES_3.id, cnLang.id, 'SCP系列 3', '/scp-series-3'),
(SERIES_4.id, cnLang.id, 'SCP系列 4', '/scp-series-4'),
(SERIES_5.id, cnLang.id, 'SCP系列 5', '/scp-series-5');

create table if not exists article_categories__to__articles
(
    id                  BIGSERIAL not null,
    article_category_id bigint    not null,
    article_id          bigint    not null,
    created             timestamp,
    updated             timestamp,
    PRIMARY KEY (id)
);

ALTER TABLE article_categories__to__articles
    drop constraint IF EXISTS fk_article_category_id__to__article_categories CASCADE;
alter table article_categories__to__articles
    add constraint fk_article_category_id__to__article_categories
        foreign key (article_category_id)
            REFERENCES article_categories (id);

ALTER TABLE article_categories__to__articles
    drop constraint IF EXISTS fk_article_id__to__articles CASCADE;
alter table article_categories__to__articles
    add constraint fk_article_id__to__articles
        foreign key (article_id)
            REFERENCES articles (id);