CREATE TABLE IF NOT EXISTS tags
(
  id      bigserial not null,
  created timestamp,
  updated timestamp,
  primary key (id)
);

CREATE TABLE IF NOT EXISTS tags_langs
(
  id      bigserial              not null,
  tag_id  bigint,
  lang_id character varying(255) not null,
  title   text                   not null,
  created timestamp,
  updated timestamp,
  primary key (id),
  constraint fk_tags foreign key (tag_id) REFERENCES tags (id),
  constraint fk_langs foreign key (lang_id) REFERENCES langs (id),
  unique (lang_id, title)
);

CREATE TABLE IF NOT EXISTS tags_articles_langs
(
  id                  bigserial not null,
  tag_for_lang_id     bigint    not null,
  article_for_lang_id bigint    not null,
  created             timestamp,
  updated             timestamp,
  primary key (id),
  constraint fk_tags foreign key (tag_for_lang_id) REFERENCES tags_langs (id),
  constraint fk_langs foreign key (article_for_lang_id) REFERENCES articles_langs (id),
  unique (tag_for_lang_id, article_for_lang_id)
);