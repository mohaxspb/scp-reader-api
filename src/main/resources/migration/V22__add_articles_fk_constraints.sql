ALTER TABLE articles_langs
    drop constraint IF EXISTS fk_article_id__to_articles CASCADE;
alter table articles_langs
    add constraint fk_article_id__to_articles
        foreign key (article_id)
            REFERENCES articles (id);

ALTER TABLE articles_langs
    drop constraint IF EXISTS fk_lang_id__to_langs CASCADE;
alter table articles_langs
    add constraint fk_lang_id__to_langs
        foreign key (lang_id)
            REFERENCES langs (id);

ALTER TABLE article_types_to_titles_for_lang
    drop constraint IF EXISTS fk_article_type_id_to_article_type CASCADE;
alter table article_types_to_titles_for_lang
    add constraint fk_article_type_id_to_article_type
        foreign key (article_type_id)
            REFERENCES article_types (id);

ALTER TABLE article_types_to_titles_for_lang
    drop constraint IF EXISTS fk_lang_id_to_langs CASCADE;
alter table article_types_to_titles_for_lang
    add constraint fk_lang_id_to_langs
        foreign key (lang_id)
            REFERENCES langs (id);

ALTER TABLE articles_article_types
    drop constraint IF EXISTS fk_article_id_to_articles CASCADE;
alter table articles_article_types
    add constraint fk_article_id_to_articles
        foreign key (article_id)
            REFERENCES articles (id);

ALTER TABLE articles_article_types
    drop constraint IF EXISTS fk_article_type_id_to_articles CASCADE;
alter table articles_article_types
    add constraint fk_article_type_id_to_articles
        foreign key (article_type_id)
            REFERENCES article_types (id);

ALTER TABLE articles_langs_to_articles_langs
    drop constraint IF EXISTS fk_article_for_lang_id__to__articles_langs CASCADE;
alter table articles_langs_to_articles_langs
    add constraint fk_article_for_lang_id__to__articles_langs
        foreign key (article_for_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE articles_langs_to_articles_langs
    drop constraint IF EXISTS fk_parent_article_for_lang_id__to__articles_langs CASCADE;
alter table articles_langs_to_articles_langs
    add constraint fk_parent_article_for_lang_id__to__articles_langs
        foreign key (parent_article_for_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE tags_articles_langs
    drop constraint IF EXISTS fk_article_for_lang_id__to__articles_langs CASCADE;
alter table tags_articles_langs
    add constraint fk_article_for_lang_id__to__articles_langs
        foreign key (article_for_lang_id)
            REFERENCES articles_langs (id);