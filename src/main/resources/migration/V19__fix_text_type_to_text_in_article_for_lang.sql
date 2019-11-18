alter table articles_langs alter column text type text using text::text;
alter table articles_langs alter column comments_url type text using comments_url::text;
alter table articles_langs alter column url_relative type text using url_relative::text;

alter table article_types alter column image_url type text using image_url::text;

alter table articles_images alter column url type text using url::text;

alter table langs alter column site_base_url type text using site_base_url::text;

alter table users alter column avatar type text using avatar::text;
alter table users alter column facebook_id type text using facebook_id::text;
alter table users alter column full_name type text using full_name::text;
alter table users alter column google_id type text using google_id::text;
alter table users alter column name_first type text using name_first::text;
alter table users alter column name_second type text using name_second::text;
alter table users alter column name_third type text using name_third::text;
alter table users alter column password type text using password::text;
alter table users alter column username type text using username::text;
alter table users alter column vk_id type text using vk_id::text;

alter table users_langs alter column firebase_uid type text using firebase_uid::text;