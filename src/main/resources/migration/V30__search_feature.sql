create table if not exists article_to_lang_text_parts_vector
(
    id           bigserial not null,

    text_part_id bigint    not null,
    vector       tsvector  not null,
    primary key (id)
);

ALTER TABLE article_to_lang_text_parts_vector
    drop constraint IF EXISTS fk_text_part_id__to__article_to_lang_text_parts CASCADE;
alter table article_to_lang_text_parts_vector
    add constraint fk_text_part_id__to__article_to_lang_text_parts
        foreign key (text_part_id)
            REFERENCES article_to_lang_text_parts (id) ON DELETE CASCADE;

drop index if exists idx_gin_document;
CREATE INDEX idx_gin_document
    ON article_to_lang_text_parts_vector
        USING gin ("vector");

-- add postgres languages to langs table
ALTER TABLE langs
    ADD COLUMN IF NOT EXISTS postgres_lang TEXT default 'simple';

update langs as l
set postgres_lang = c.postgres_lang
from (
         values ('ru', 'russian'),
                ('en', 'english'),
                ('pl', 'simple'),
                ('de', 'german'),
                ('fr', 'french'),
                ('it', 'italian'),
                ('es', 'spanish'),
                ('pt', 'portuguese'),
                ('zh', 'simple')
     ) as c(lang_code, postgres_lang)
where c.lang_code = l.lang_code;

-- fill vectors for existing rows
INSERT INTO article_to_lang_text_parts_vector(text_part_id, vector)
select t.id,
       coalesce(to_tsvector(l.postgres_lang::regconfig, t.data), to_tsvector(l.postgres_lang::regconfig, ''))
from article_to_lang_text_parts t
         join articles_langs al on t.article_to_lang_id = al.id
         join langs l on al.lang_id = l.id
where t.type in ('TEXT', 'IMAGE_TITLE', 'TABLE')
ON CONFLICT DO NOTHING;

-- trigger, that inserts vector for each text part
CREATE OR REPLACE FUNCTION insert_vector() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT INTO article_to_lang_text_parts_vector(text_part_id, vector)
    VALUES (new.id,
            to_tsvector(
                    (select langs.postgres_lang
                     from langs
                     where id = (select lang_id from articles_langs where id = new.article_to_lang_id))::regconfig,
                    new.data
                ));

    RETURN new;
END;
$BODY$
    language plpgsql;


DROP TRIGGER IF EXISTS text_parts_vector_insert on article_to_lang_text_parts;
CREATE TRIGGER text_parts_vector_insert
    AFTER INSERT
    ON article_to_lang_text_parts
    FOR EACH ROW
    WHEN (new.type = 'TEXT' or new.type = 'IMAGE_TITLE')
EXECUTE PROCEDURE
    insert_vector();


-- titles
create table if not exists article_to_lang_titles_vector
(
    id                 bigserial not null,

    article_to_lang_id bigint    not null,
    vector             tsvector  not null,
    primary key (id)
);

ALTER TABLE article_to_lang_titles_vector
    drop constraint IF EXISTS fk_text_part_id__to__article_to_langs CASCADE;
alter table article_to_lang_titles_vector
    add constraint fk_text_part_id__to__article_to_langs
        foreign key (article_to_lang_id)
            REFERENCES articles_langs (id) ON DELETE CASCADE;

drop index if exists idx_gin_article_to_lang_titles;
CREATE INDEX idx_gin_article_to_lang_titles
    ON article_to_lang_titles_vector
        USING gin ("vector");

-- fill vectors for existing rows
INSERT INTO article_to_lang_titles_vector(article_to_lang_id, vector)
select al.id,
       coalesce(to_tsvector(l.postgres_lang::regconfig, al.title), to_tsvector(l.postgres_lang::regconfig, ''))
from articles_langs al
         join langs l on al.lang_id = l.id
ON CONFLICT DO NOTHING;


-- trigger, that inserts vector for each text part
CREATE OR REPLACE FUNCTION insert_titles_vector() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT INTO article_to_lang_titles_vector(article_to_lang_id, vector)
    VALUES (new.id,
            to_tsvector(
                    (select langs.postgres_lang
                     from langs
                     where id = new.lang_id)::regconfig,
                    new.title
                ));

    RETURN new;
END;
$BODY$
    language plpgsql;


DROP TRIGGER IF EXISTS article_to_langs_vector_insert on articles_langs;
CREATE TRIGGER article_to_langs_vector_insert
    AFTER INSERT
    ON articles_langs
    FOR EACH ROW
EXECUTE PROCEDURE
    insert_titles_vector();