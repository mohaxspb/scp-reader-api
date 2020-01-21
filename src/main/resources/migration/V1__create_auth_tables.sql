create table if not exists oauth_client_details
(
    client_id               varchar(255) not null,
    access_token_validity   integer      not null,
    additional_information  varchar(255),
    authorities             varchar(255),
    authorized_grant_types  varchar(255),
    autoapprove             varchar(255),
    client_secret           varchar(255),
    refresh_token_validity  integer      not null,
    resource_ids            varchar(255),
    scope                   varchar(255),
    web_server_redirect_uri varchar(255),
    updated                 timestamp,
    created                 timestamp,
    primary key (client_id)
);


create table if not exists oauth_access_token
(
    token_id          varchar(255) not null,
    authentication    bytea,
    authentication_id varchar(255),
    client_id         varchar(255),
    refresh_token     varchar(255),
    token             bytea,
    user_name         varchar(255),
    updated           timestamp,
    created           timestamp,
    primary key (token_id)
);

ALTER TABLE oauth_access_token
    drop constraint IF EXISTS fk_oauth_access_token__to__oauth_client_details CASCADE;
alter table oauth_access_token
    add constraint fk_oauth_access_token__to__oauth_client_details
        foreign key (client_id)
            REFERENCES oauth_client_details (client_id);


create table if not exists oauth_client_token
(
    token_id          varchar(255) not null,
    authentication_id varchar(255),
    client_id         varchar(255),
    token             bytea,
    updated           timestamp,
    created           timestamp,
    user_name         varchar(255),
    primary key (token_id)
);

ALTER TABLE oauth_access_token
    drop constraint IF EXISTS fk_oauth_client_token__to__oauth_client_details CASCADE;
alter table oauth_access_token
    add constraint fk_oauth_client_token__to__oauth_client_details
        foreign key (client_id)
            REFERENCES oauth_client_details (client_id);


create table if not exists oauth_refresh_token
(
    token_id       varchar(255) not null,
    authentication bytea,
    token          bytea,
    updated        timestamp default CURRENT_TIMESTAMP,
    created        timestamp,
    primary key (token_id)
);


create table if not exists users
(
    id                    bigserial not null,
    username              text      not null,
    password              text      not null,
    full_name             text,
    avatar                text,

    enabled               boolean   not null default true,

    score                 integer   not null default 0,
    level_num             integer,
    cur_level_score       integer,
    score_to_next_level   integer,

    sign_in_reward_gained boolean,

    facebook_id           text,
    google_id             text,
    vk_id                 text,

    main_lang_id          text      not null default 'en',

    created               timestamp,
    updated               timestamp,
    primary key (id)
);

alter table users
    drop constraint if exists username_unique;
alter table users
    add constraint username_unique unique (username);


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
    authority text      not null,
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


create table if not exists langs
(
    id        varchar(255) not null,
    lang_code varchar(255),
    primary key (id)
);

alter table langs
    drop constraint if exists lang_code_unique;
alter table langs
    add constraint lang_code_unique unique (lang_code);

create table if not exists site_base_urls__to__langs
(
    id            bigserial    not null,
    lang_id       varchar(255) not null,
    site_base_url varchar(255) not null,
    primary key (id)
);

ALTER TABLE site_base_urls__to__langs
    drop constraint IF EXISTS fk_lang_id__to__langs CASCADE;
alter table site_base_urls__to__langs
    add constraint fk_lang_id__to__langs
        foreign key (lang_id)
            REFERENCES langs (id);

alter table site_base_urls__to__langs
    drop constraint if exists site_base_url_unique;
alter table site_base_urls__to__langs
    add constraint site_base_url_unique unique (site_base_url);


create table if not exists users_langs
(
    id           bigserial    not null,
    lang_id      varchar(255) not null,
    user_id      bigint       not null,
    firebase_uid text,
    created      timestamp,
    updated      timestamp,
    primary key (id)
);

alter table users_langs
    drop constraint if exists user_id_and_lang_id_unique;
alter table users_langs
    add constraint user_id_and_lang_id_unique unique (user_id, lang_id);

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
