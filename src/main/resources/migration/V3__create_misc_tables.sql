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
