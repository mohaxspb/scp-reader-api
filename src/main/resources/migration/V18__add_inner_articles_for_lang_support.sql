ALTER TABLE articles_langs ADD COLUMN IF NOT EXISTS parent_article_for_lang_id bigint;

alter table articles_langs
    add constraint fk_article_langs_article_langs
    foreign key (parent_article_for_lang_id)
    REFERENCES articles_langs (id);