-- remove redundant article_type_id from articles
-- change PK for articles-articles_types to surrogate one
-- add table for article types titles for langs

alter table articles
    drop column if exists article_type_id;

alter table articles_article_types
    drop constraint if exists articles_article_types_pkey;
alter table articles_article_types
    drop constraint if exists articles_article_types_unique;
alter table articles_article_types
    add constraint articles_article_types_unique
        unique (article_id, article_type_id);
alter table articles_article_types
    add column if not exists id bigserial not null;
alter table articles_article_types
    add primary key (id);

CREATE TABLE IF NOT EXISTS article_types_to_titles_for_lang
(
    id              bigserial not null,
    article_type_id bigint    not null,
    lang_id         varchar   not null,
    title           text      not null,
    created         timestamp,
    updated         timestamp,
    primary key (id),
    unique (article_type_id, lang_id)
);