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

create table if not exists article_types
(
    id        bigserial not null,
    image_url text,
    primary key (id)
);

create table if not exists articles
(
    id              bigserial not null,
    article_type_id bigint,
    primary key (id)
);

create table if not exists articles_article_types
(
    article_id      bigint not null,
    article_type_id bigint not null,
    created         timestamp,
    updated         timestamp,
    primary key (article_id, article_type_id)
);

create table if not exists articles_langs
(
    id              serial       not null,
    article_id      bigint       not null,
    lang_id         varchar(255) not null,
    url_relative    text         not null,
    comments_url    text,
    created         timestamp,
    created_on_site timestamp,
    rating          integer,
    text            text,
    title           varchar(255),
    updated         timestamp,
    updated_on_site timestamp,
    primary key (id)
);

create table if not exists articles_images
(
    id                  serial not null,
    url                 text   not null,
    created             timestamp,
    updated             timestamp,
    article_for_lang_id bigint
        constraint fk_article_image_article_langs
            references articles_langs,
    primary key (id)
);

create unique index if not exists articles_langs_unique
    on articles_langs (article_id, lang_id, url_relative);

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


create table if not exists firebase_data_update_date
(
    id      bigserial not null,
    lang_id varchar(255),
    updated timestamp,
    primary key (id)
);

create table if not exists langs
(
    id            varchar(255) not null,
    lang_code     varchar(255),
    site_base_url text,
    primary key (id)
);

create table if not exists oauth_access_token
(
    token_id          varchar(255) not null,
    authentication    bytea,
    authentication_id varchar(255),
    client_id         varchar(255),
    created           timestamp,
    refresh_token     varchar(255),
    token             bytea,
    updated           timestamp,
    user_name         varchar(255),
    primary key (token_id)
);

create table if not exists oauth_client_details
(
    client_id               varchar(255) not null,
    access_token_validity   integer      not null,
    additional_information  varchar(255),
    authorities             varchar(255),
    authorized_grant_types  varchar(255),
    autoapprove             varchar(255),
    client_secret           varchar(255),
    created                 timestamp,
    refresh_token_validity  integer      not null,
    resource_ids            varchar(255),
    scope                   varchar(255),
    updated                 timestamp,
    web_server_redirect_uri varchar(255),
    primary key (client_id)
);

create table if not exists oauth_client_token
(
    token_id          varchar(255) not null,
    authentication_id varchar(255),
    client_id         varchar(255),
    created           timestamp,
    token             bytea,
    updated           timestamp,
    user_name         varchar(255),
    primary key (token_id)
);

create table if not exists oauth_refresh_token
(
    token_id       varchar(255) not null,
    authentication bytea,
    created        timestamp,
    token          bytea,
    updated        timestamp default CURRENT_TIMESTAMP,
    primary key (token_id)
);


create table if not exists users
(
    id                    bigserial not null,
    avatar                text,
    created               timestamp,
    cur_level_score       integer,
    enabled               boolean   not null,
    full_name             text,
    level_num             integer,
    password              text,
    username              text
        constraint uk_r43af9ap4edm43mmtq01oddj6
            unique,
    name_first            text,
    name_second           text,
    name_third            text,
    score                 integer,
    score_to_next_level   integer,
    sign_in_reward_gained boolean,
    updated               timestamp,
    facebook_id           text,
    google_id             text,
    vk_id                 text,
    main_lang_id          text,
    primary key (id)
);


create table if not exists authorities
(
    authority text not null,
    created   timestamp,
    updated   timestamp,
    primary key (authority)
);

ALTER TABLE authorities
    DROP CONSTRAINT IF EXISTS check_upper_authority;
ALTER TABLE authorities
    ADD CONSTRAINT check_upper_authority CHECK (UPPER(authority) = authority);

INSERT INTO authorities (authority)
VALUES ('ADMIN'),
       ('BANNER'),
       ('USER')
ON CONFLICT DO NOTHING;


create table if not exists authorities__to__users
(
    id        bigserial not null,
    authority bigint    not null,
    user_id   bigint    not null,
    created   timestamp,
    updated   timestamp,
    primary key (id)
);

alter table authorities__to__users
    drop constraint if exists authority_id_and_user_id_unique;
alter table authorities__to__users
    add constraint authority_id_and_user_id_unique unique (authority, user_id);

ALTER TABLE authorities__to__users
    drop constraint IF EXISTS fkk91upmbueyim93v469wj7b2qh CASCADE;
ALTER TABLE authorities__to__users
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table authorities__to__users
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);

ALTER TABLE authorities__to__users
    drop constraint IF EXISTS fk_authority_id__to__authorities CASCADE;
alter table authorities__to__users
    add constraint fk_authority_id__to__authorities
        foreign key (authority)
            REFERENCES authorities (authority);


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


create table if not exists users_langs
(
    lang_id      varchar(255) not null,
    user_id      bigint       not null,
    created      timestamp,
    firebase_uid text,
    updated      timestamp,
    constraint users_langs_pkey
        primary key (lang_id, user_id)
);

ALTER TABLE users_langs
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table users_langs
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);

ALTER TABLE users_langs
    drop constraint IF EXISTS fk_lang_id__to__langs CASCADE;
alter table users_langs
    add constraint fk_lang_id__to__langs
        foreign key (lang_id)
            REFERENCES langs (id);


create table if not exists tags
(
    id      bigserial not null,
    created timestamp,
    updated timestamp,
    primary key (id)
);

create table if not exists tags_langs
(
    id      bigserial    not null
        constraint tags_langs_pkey
            primary key,
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
        unique (lang_id, title)
);

create table if not exists tags_articles_langs
(
    id                  bigserial not null
        constraint tags_articles_langs_pkey
            primary key,
    tag_for_lang_id     bigint    not null
        constraint fk_tags
            references tags_langs,
    article_for_lang_id bigint    not null
        constraint fk_langs
            references articles_langs,
    created             timestamp,
    updated             timestamp,
    constraint tags_articles_langs_tag_for_lang_id_article_for_lang_id_key
        unique (tag_for_lang_id, article_for_lang_id)
);

create table if not exists articles_langs_to_articles_langs
(
    id                         bigserial not null
        constraint articles_langs_to_articles_langs_pkey
            primary key,
    parent_article_for_lang_id bigint    not null
        constraint fk_parent_article_for_lang
            references articles_langs,
    article_for_lang_id        bigint    not null
        constraint fk_article_for_lang
            references articles_langs,
    created                    timestamp,
    updated                    timestamp,
    constraint articles_langs_to_articles_la_parent_article_for_lang_id_ar_key
        unique (parent_article_for_lang_id, article_for_lang_id)
);
