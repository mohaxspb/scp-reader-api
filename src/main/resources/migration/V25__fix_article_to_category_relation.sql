-- we'll use article for lang to prevent frequent data rewriting for articles lists
ALTER TABLE IF EXISTS article_categories__to__articles
    RENAME TO article_categories_to_lang__to__articles_to_lang;

ALTER SEQUENCE if exists article_categories__to__articles_id_seq RENAME TO
    article_categories_to_lang__to__articles_to_lang_id_seq;

ALTER TABLE IF EXISTS article_category_titles_for_langs
    RENAME TO article_categories__to__langs;

ALTER SEQUENCE if exists article_category_titles_for_langs_id_seq RENAME TO
    article_categories__to__langs_id_seq;

-- change article_id to article_to_lang_id
ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    drop constraint IF EXISTS fk_article_id__to__articles CASCADE;

alter table if exists article_categories_to_lang__to__articles_to_lang
    drop column if exists article_id;

ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    ADD COLUMN IF NOT EXISTS article_to_lang_id bigint;

-- change category_id to category_to_lang_id
ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    drop constraint IF EXISTS fk_article_category_id__to__article_categories CASCADE;

alter table if exists article_categories_to_lang__to__articles_to_lang
    drop column if exists article_category_id;

ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    ADD COLUMN IF NOT EXISTS article_category_to_lang_id bigint;

-- change fk constraints
alter table article_categories_to_lang__to__articles_to_lang
    add constraint fk_article_to_lang_id__to__articles_langs
        foreign key (article_to_lang_id)
            REFERENCES articles_langs (id);

alter table article_categories_to_lang__to__articles_to_lang
    add constraint fk_article_category_to_lang_id__to__article_categories_to_langs
        foreign key (article_category_to_lang_id)
            REFERENCES article_categories__to__langs (id);

-- add order column
ALTER TABLE if exists article_categories_to_lang__to__articles_to_lang
    ADD COLUMN IF NOT EXISTS order_in_category smallint;

-- add unique constraint
alter table if exists article_categories_to_lang__to__articles_to_lang
    drop constraint if exists article_category_order_unique;
alter table article_categories_to_lang__to__articles_to_lang
    add constraint article_category_order_unique
        unique (article_category_to_lang_id, article_to_lang_id, order_in_category);