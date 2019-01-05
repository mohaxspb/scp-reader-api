CREATE TABLE IF NOT EXISTS articles_images
(
     article_url_relative        text,
     article_lang_id             character varying(255),
     article_id                  bigint,
     url                         TEXT NOT NULL,
     created                     TIMESTAMP,
     updated                     TIMESTAMP,
     FOREIGN KEY (article_id, article_lang_id, article_url_relative) REFERENCES articles_langs (article_id, lang_id, url_relative),
     PRIMARY KEY (article_url_relative, article_lang_id, article_id, url)
);