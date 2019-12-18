alter table articles_langs
    add column if not exists has_iframe_tag boolean not null default false;