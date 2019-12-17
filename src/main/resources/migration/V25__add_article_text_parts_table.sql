create table if not exists article_to_lang_text_parts
(
    id                 BIGSERIAL not null,
    article_to_lang_id bigint    not null,
    parent_id          bigint,
    type               text      not null,
    data               text,
    order_in_text      bigint    not null,
    created            timestamp,
    updated            timestamp,
    PRIMARY KEY (id)
);

alter table article_to_lang_text_parts
    drop constraint if exists article_id_and_order_in_text_unique;

alter table article_to_lang_text_parts
    add constraint article_id_and_order_in_text_unique unique (article_to_lang_id, order_in_text, parent_id);

ALTER TABLE article_to_lang_text_parts
    drop constraint IF EXISTS fk_article_to_lang_id__to__articles_langs CASCADE;
alter table article_to_lang_text_parts
    add constraint fk_article_to_lang_id__to__articles_langs
        foreign key (article_to_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE article_to_lang_text_parts
    drop constraint IF EXISTS fk_id__to__article_to_lang_text_parts CASCADE;
alter table article_to_lang_text_parts
    add constraint fk_id__to__article_to_lang_text_parts
        foreign key (parent_id)
            REFERENCES article_to_lang_text_parts (id);
