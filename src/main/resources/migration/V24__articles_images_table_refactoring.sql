create table articles_images_temp as
select distinct on (url, article_for_lang_id) *
from articles_images;

TRUNCATE articles_images;

--doesn't work. Maybe this would work on newer versions of postgres
-- alter table articles_images
--     add constraint articles_images__article_for_lang_id_and_url_unique unique (article_for_lang_id, md5(url));

drop index if exists articles_images__article_for_lang_id_and_url_unique__index cascade;
create unique index articles_images__article_for_lang_id_and_url_unique__index
    on articles_images (article_for_lang_id, md5(url));

INSERT INTO articles_images
SELECT *
FROM articles_images_temp;

drop TABLE articles_images_temp;