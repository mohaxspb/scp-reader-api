ALTER TABLE articles_langs drop constraint IF EXISTS articles_langs_pkey CASCADE;
ALTER TABLE articles_langs ADD COLUMN IF NOT EXISTS id SERIAL PRIMARY KEY;
create unique index articles_langs_unique on articles_langs (article_id, lang_id, url_relative);

ALTER TABLE articles_images drop constraint IF EXISTS articles_images_pkey CASCADE;
ALTER TABLE articles_images drop constraint IF EXISTS articles_images_article_id_fkey CASCADE;
ALTER TABLE articles_images ADD COLUMN IF NOT EXISTS id SERIAL PRIMARY KEY;
ALTER TABLE articles_images ADD COLUMN IF NOT EXISTS article_for_lang_id BIGINT;
ALTER TABLE articles_images
  drop if exists article_lang_id,
  drop if exists article_id,
  drop if exists article_url_relative;
ALTER TABLE articles_images drop constraint IF EXISTS fk_article_image_article_langs CASCADE;
alter table articles_images
    add constraint fk_article_image_article_langs
    foreign key (article_for_lang_id)
    REFERENCES articles_langs (id);