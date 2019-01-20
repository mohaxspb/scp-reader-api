CREATE TABLE IF NOT EXISTS articles_langs_to_articles_langs
(
  id                         bigserial not null,
  parent_article_for_lang_id bigint    not null,
  article_for_lang_id        bigint    not null,
  created                    timestamp,
  updated                    timestamp,
  primary key (id),
  constraint fk_parent_article_for_lang foreign key (parent_article_for_lang_id) REFERENCES articles_langs (id),
  constraint fk_article_for_lang foreign key (article_for_lang_id) REFERENCES articles_langs (id),
  unique (parent_article_for_lang_id, article_for_lang_id)
);