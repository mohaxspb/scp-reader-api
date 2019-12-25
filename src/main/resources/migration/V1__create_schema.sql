create table if not exists article_types
(
    id        bigserial not null,
    image_url text,
    primary key (id)
);

create table if not exists articles
(
    id bigserial not null,
    primary key (id)
);

create table if not exists articles_article_types
(
    id bigserial not null,
    article_id      bigint not null,
    article_type_id bigint not null,
    created         timestamp,
    updated         timestamp,
    primary key (id)
);

alter table articles_article_types
    drop constraint if exists articles_article_types_unique;
alter table articles_article_types
    add constraint articles_article_types_unique
        unique (article_id, article_type_id);

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

CREATE TABLE IF NOT EXISTS article_types__to__langs
(
    id              bigserial not null,
    article_type_id bigint    not null,
    lang_id         varchar   not null,
    title           text      not null,
    created         timestamp,
    updated         timestamp,
    primary key (id),
    unique (article_type_id, lang_id)
);

ALTER TABLE article_types__to__langs
    drop constraint IF EXISTS fk_article_type_id_to_article_type CASCADE;
alter table article_types__to__langs
    add constraint fk_article_type_id_to_article_type
        foreign key (article_type_id)
            REFERENCES article_types (id);

ALTER TABLE article_types__to__langs
    drop constraint IF EXISTS fk_lang_id_to_langs CASCADE;
alter table article_types__to__langs
    add constraint fk_lang_id_to_langs
        foreign key (lang_id)
            REFERENCES langs (id);

create table if not exists articles_langs
(
    id              bigserial    not null,
    article_id      bigint       not null,
    lang_id         varchar(255) not null,
    url_relative    text         not null,
    title           varchar(255),
    rating          integer,
    text            text,
    comments_url    text,
    has_iframe_tag boolean not null default false,
    updated_on_site timestamp,
    created_on_site timestamp,
    updated         timestamp,
    created         timestamp,
    primary key (id)
);

DROP INDEX IF EXISTS articles_langs_unique;
create unique index if not exists articles_langs_unique on articles_langs (article_id, lang_id, url_relative);

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


CREATE TABLE IF NOT EXISTS articles_langs__to__articles_langs
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


create table if not exists articles_images
(
    id                  bigserial not null,
    url                 text      not null,
    created             timestamp,
    updated             timestamp,
    article_for_lang_id bigint
        constraint fk_article_image_article_langs
            references articles_langs,
    primary key (id)
);


create table if not exists banners
(
    id              bigserial    not null,
    image_url       varchar(255),
    logo_url        varchar(255),
    title           varchar(255) not null,
    sub_title       varchar(255) not null,
    cta_button_text varchar(255) not null,
    redirect_url    varchar(255) not null,
    banner_type     varchar(255) not null,
    enabled         boolean,
    author_id       bigint,
    created         timestamp,
    updated         timestamp,
    primary key (id)
);


create table if not exists read__articles_to_lang__to__users
(
    id                 bigserial not null,
    article_to_lang_id bigint    not null,
    user_id            bigint    not null,
    created            timestamp,
    updated            timestamp,
    primary key (id)
);

ALTER TABLE read__articles_to_lang__to__users
    drop constraint IF EXISTS fk_article_to_lang_id__to__articles_langs CASCADE;
alter table read__articles_to_lang__to__users
    add constraint fk_article_to_lang_id__to__articles_langs
        foreign key (article_to_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE read__articles_to_lang__to__users
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table read__articles_to_lang__to__users
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);


create table if not exists favorite__articles_to_lang__to__users
(
    id                 bigserial not null,
    article_to_lang_id bigint    not null,
    user_id            bigint    not null,
    created            timestamp,
    updated            timestamp,
    primary key (id)
);

ALTER TABLE favorite__articles_to_lang__to__users
    drop constraint IF EXISTS fk_article_to_lang_id__to__articles_langs CASCADE;
alter table favorite__articles_to_lang__to__users
    add constraint fk_article_to_lang_id__to__articles_langs
        foreign key (article_to_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE favorite__articles_to_lang__to__users
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table favorite__articles_to_lang__to__users
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);


create table if not exists tags
(
    id      bigserial not null,
    created timestamp,
    updated timestamp,
    primary key (id)
);

create table if not exists tags_langs
(
    id      bigserial    not null,
    tag_id  bigint
        constraint fk_tags
            references tags,
    lang_id varchar(255) not null
        constraint fk_langs
            references langs,
    title   text         not null,
    created timestamp,
    updated timestamp,
    constraint tags_langs_lang_id_title_key
        unique (lang_id, title),
    primary key (id)
);

create table if not exists tags_articles_langs
(
    id                  bigserial not null,
    tag_for_lang_id     bigint    not null,
    article_for_lang_id bigint    not null,
    created             timestamp,
    updated             timestamp,
    constraint tags_articles_langs_tag_for_lang_id_article_for_lang_id_key
        unique (tag_for_lang_id, article_for_lang_id),
    primary key (id)
);

ALTER TABLE tags_articles_langs
    drop constraint IF EXISTS fk_article_for_lang_id__to__articles_langs CASCADE;
alter table tags_articles_langs
    add constraint fk_article_for_lang_id__to__articles_langs
        foreign key (article_for_lang_id)
            REFERENCES articles_langs (id);

ALTER TABLE tags_articles_langs
    drop constraint IF EXISTS fk_tag_for_lang_id__to__tags_langs CASCADE;
alter table tags_articles_langs
    add constraint fk_tag_for_lang_id__to__tags_langs
        foreign key (tag_for_lang_id)
            REFERENCES tags_langs (id);


create table if not exists firebase_data_update_date
(
    id      bigserial not null,
    lang_id varchar(255),
    updated timestamp,
    primary key (id)
);

create table if not exists android_products
(
    id                   bigserial not null,
    android_package      varchar(255),
    consumption_state    integer,
    created              timestamp,
    order_id             varchar(255),
    purchase_state       integer,
    purchase_time_millis timestamp,
    purchase_token       varchar(255),
    purchase_type        integer,
    updated              timestamp,
    primary key (id)
);

create table if not exists android_subscriptions
(
    id                            bigserial not null,
    android_package               varchar(255),
    auto_renewing                 boolean,
    created                       timestamp,
    expiry_time_millis            timestamp,
    linked_purchase_token         varchar(255),
    order_id                      varchar(255),
    price_amount_micros           bigint,
    price_currency_code           varchar(255),
    purchase_token                varchar(255),
    start_time_millis             timestamp,
    updated                       timestamp,
    user_cancellation_time_millis timestamp,
    primary key (id)
);


create table if not exists users_android_products
(
    android_product_id bigint not null,
    user_id            bigint not null,
    created            timestamp,
    updated            timestamp,
    primary key (android_product_id, user_id)
);

ALTER TABLE users_android_products
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table users_android_products
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);


create table if not exists users_android_subscriptions
(
    android_subscription_id bigint not null,
    user_id                 bigint not null,
    created                 timestamp,
    updated                 timestamp,
    primary key (android_subscription_id, user_id)
);

ALTER TABLE users_android_subscriptions
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table users_android_subscriptions
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);
