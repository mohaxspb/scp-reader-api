create table if not exists android_products
(
    id bigserial not null
        constraint android_products_pkey
            primary key,
    android_package varchar(255),
    consumption_state integer,
    created timestamp,
    order_id varchar(255),
    purchase_state integer,
    purchase_time_millis timestamp,
    purchase_token varchar(255),
    purchase_type integer,
    updated timestamp
);

create table if not exists android_subscriptions
(
    id bigserial not null
        constraint android_subscriptions_pkey
            primary key,
    android_package varchar(255),
    auto_renewing boolean,
    created timestamp,
    expiry_time_millis timestamp,
    linked_purchase_token varchar(255),
    order_id varchar(255),
    price_amount_micros bigint,
    price_currency_code varchar(255),
    purchase_token varchar(255),
    start_time_millis timestamp,
    updated timestamp,
    user_cancellation_time_millis timestamp
);

create table if not exists article_types
(
    id bigserial not null
        constraint article_types_pkey
            primary key,
    image_url text
);

create table if not exists articles
(
    id bigserial not null
        constraint articles_pkey
            primary key,
    article_type_id bigint
);

create table if not exists articles_article_types
(
    article_id bigint not null,
    article_type_id bigint not null,
    created timestamp,
    updated timestamp,
    constraint articles_article_types_pkey
        primary key (article_id, article_type_id)
);

create table if not exists articles_langs
(
    article_id bigint not null,
    lang_id varchar(255) not null,
    url_relative text not null,
    comments_url text,
    created timestamp,
    created_on_site timestamp,
    rating integer,
    text text,
    title varchar(255),
    updated timestamp,
    updated_on_site timestamp,
    id serial not null
        constraint articles_langs_pkey
            primary key
);

create table if not exists articles_images
(
    url text not null,
    created timestamp,
    updated timestamp,
    id serial not null
        constraint articles_images_pkey
            primary key,
    article_for_lang_id bigint
        constraint fk_article_image_article_langs
            references articles_langs
);

create unique index if not exists articles_langs_unique
    on articles_langs (article_id, lang_id, url_relative);

create table if not exists banners
(
    id bigserial not null
        constraint banners_pkey
            primary key,
    image_url varchar(255),
    logo_url varchar(255),
    title varchar(255) not null,
    sub_title varchar(255) not null,
    cta_button_text varchar(255) not null,
    redirect_url varchar(255) not null,
    banner_type varchar(255) not null,
    enabled boolean,
    author_id bigint,
    created timestamp,
    updated timestamp
);

create table if not exists favorite_articles_by_lang
(
    article_id bigint not null,
    lang_id varchar(255) not null,
    user_id bigint not null,
    created timestamp,
    is_favorite boolean,
    updated timestamp,
    constraint favorite_articles_by_lang_pkey
        primary key (article_id, lang_id, user_id)
);

create table if not exists firebase_data_update_date
(
    id bigserial not null
        constraint firebase_data_update_date_pkey
            primary key,
    lang_id varchar(255),
    updated timestamp
);

create table if not exists flyway_schema_history
(
    installed_rank integer not null
        constraint flyway_schema_history_pk
            primary key,
    version varchar(50),
    description varchar(200) not null,
    type varchar(20) not null,
    script varchar(1000) not null,
    checksum integer,
    installed_by varchar(100) not null,
    installed_on timestamp default now() not null,
    execution_time integer not null,
    success boolean not null
);

create table if not exists langs
(
    id varchar(255) not null
        constraint langs_pkey
            primary key,
    lang_code varchar(255),
    site_base_url text
);

create table if not exists oauth_access_token
(
    token_id varchar(255) not null
        constraint oauth_access_token_pkey
            primary key,
    authentication bytea,
    authentication_id varchar(255),
    client_id varchar(255),
    created timestamp,
    refresh_token varchar(255),
    token bytea,
    updated timestamp,
    user_name varchar(255)
);

create table if not exists oauth_client_details
(
    client_id varchar(255) not null
        constraint oauth_client_details_pkey
            primary key,
    access_token_validity integer not null,
    additional_information varchar(255),
    authorities varchar(255),
    authorized_grant_types varchar(255),
    autoapprove varchar(255),
    client_secret varchar(255),
    created timestamp,
    refresh_token_validity integer not null,
    resource_ids varchar(255),
    scope varchar(255),
    updated timestamp,
    web_server_redirect_uri varchar(255)
);

create table if not exists oauth_client_token
(
    token_id varchar(255) not null
        constraint oauth_client_token_pkey
            primary key,
    authentication_id varchar(255),
    client_id varchar(255),
    created timestamp,
    token bytea,
    updated timestamp,
    user_name varchar(255)
);

create table if not exists oauth_refresh_token
(
    token_id varchar(255) not null
        constraint oauth_refresh_token_pkey
            primary key,
    authentication bytea,
    created timestamp,
    token bytea,
    updated timestamp default CURRENT_TIMESTAMP
);

create table if not exists read_articles_by_lang
(
    article_id bigint not null,
    lang_id varchar(255) not null,
    user_id bigint not null,
    created timestamp,
    is_read boolean,
    updated timestamp,
    constraint read_articles_by_lang_pkey
        primary key (article_id, lang_id, user_id)
);

create table if not exists users
(
    id bigserial not null
        constraint users_pkey
            primary key,
    avatar text,
    created timestamp,
    cur_level_score integer,
    enabled boolean not null,
    full_name text,
    level_num integer,
    password text,
    username text
        constraint uk_r43af9ap4edm43mmtq01oddj6
            unique,
    name_first text,
    name_second text,
    name_third text,
    score integer,
    score_to_next_level integer,
    sign_in_reward_gained boolean,
    updated timestamp,
    facebook_id text,
    google_id text,
    vk_id text,
    main_lang_id text
);

create table if not exists authorities
(
    authority varchar(255) not null,
    user_id bigint not null
        constraint fkk91upmbueyim93v469wj7b2qh
            references users,
    created timestamp,
    updated timestamp,
    constraint authorities_pkey
        primary key (authority, user_id)
);

create table if not exists users_android_products
(
    android_product_id bigint not null,
    user_id bigint not null,
    created timestamp,
    updated timestamp,
    constraint users_android_products_pkey
        primary key (android_product_id, user_id)
);

create table if not exists users_android_subscriptions
(
    android_subscription_id bigint not null,
    user_id bigint not null,
    created timestamp,
    updated timestamp,
    constraint users_android_subscriptions_pkey
        primary key (android_subscription_id, user_id)
);

create table if not exists users_langs
(
    lang_id varchar(255) not null,
    user_id bigint not null,
    created timestamp,
    firebase_uid text,
    updated timestamp,
    constraint users_langs_pkey
        primary key (lang_id, user_id)
);

create table if not exists tags
(
    id bigserial not null
        constraint tags_pkey
            primary key,
    created timestamp,
    updated timestamp
);

create table if not exists tags_langs
(
    id bigserial not null
        constraint tags_langs_pkey
            primary key,
    tag_id bigint
        constraint fk_tags
            references tags,
    lang_id varchar(255) not null
        constraint fk_langs
            references langs,
    title text not null,
    created timestamp,
    updated timestamp,
    constraint tags_langs_lang_id_title_key
        unique (lang_id, title)
);

create table if not exists tags_articles_langs
(
    id bigserial not null
        constraint tags_articles_langs_pkey
            primary key,
    tag_for_lang_id bigint not null
        constraint fk_tags
            references tags_langs,
    article_for_lang_id bigint not null
        constraint fk_langs
            references articles_langs,
    created timestamp,
    updated timestamp,
    constraint tags_articles_langs_tag_for_lang_id_article_for_lang_id_key
        unique (tag_for_lang_id, article_for_lang_id)
);

create table if not exists articles_langs_to_articles_langs
(
    id bigserial not null
        constraint articles_langs_to_articles_langs_pkey
            primary key,
    parent_article_for_lang_id bigint not null
        constraint fk_parent_article_for_lang
            references articles_langs,
    article_for_lang_id bigint not null
        constraint fk_article_for_lang
            references articles_langs,
    created timestamp,
    updated timestamp,
    constraint articles_langs_to_articles_la_parent_article_for_lang_id_ar_key
        unique (parent_article_for_lang_id, article_for_lang_id)
);
