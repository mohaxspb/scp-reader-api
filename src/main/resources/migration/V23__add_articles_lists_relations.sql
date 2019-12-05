create table if not exists article_categories
(
    id            BIGSERIAL not null,
    default_title text      not null,
    created       timestamp,
    updated       timestamp,
    PRIMARY KEY (id)
);

-- todo insert predefined categories

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

-- todo insert predefined titles for langs

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