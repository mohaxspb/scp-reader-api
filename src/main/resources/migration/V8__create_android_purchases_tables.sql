create table IF NOT EXISTS android_products (
     id                   bigserial not null,
     android_package      varchar(255),
     consumption_state    int4,
     created              timestamp,
     order_id             varchar(255),
     purchase_state       int4,
     purchase_time_millis timestamp,
     purchase_token       varchar(255),
     purchase_type        int4,
     updated              timestamp,
     primary key (id)
);

create table IF NOT EXISTS android_subscriptions (
     id                            bigserial not null,
     android_package               varchar(255),
     auto_renewing                 boolean,
     created                       timestamp,
     expiry_time_millis            timestamp,
     linked_purchase_token         varchar(255),
     order_id                      varchar(255),
     price_amount_micros           int8,
     price_currency_code           varchar(255),
     purchase_token                varchar(255),
     start_time_millis             timestamp,
     updated                       timestamp,
     user_cancellation_time_millis timestamp,
     primary key (id)
);

create table IF NOT EXISTS users_android_products (
    android_product_id int8 not null,
    user_id int8 not null,
    created timestamp,
    updated timestamp,
    primary key (android_product_id, user_id)
);

create table IF NOT EXISTS users_android_subscriptions (
    android_subscription_id int8 not null,
    user_id int8 not null,
    created timestamp,
    updated timestamp,
    primary key (android_subscription_id, user_id)
);