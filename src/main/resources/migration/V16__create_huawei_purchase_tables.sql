create table if not exists huawei_products
(
    id                   bigserial not null,
    android_package      text,
    --0: consumable
    --1: non-consumable
    --2: renewable subscription
    --3: non-renewable subscription
    kind                 integer   not null,

    order_id             text,
    purchase_state       integer,
    purchase_time_millis timestamp,
    purchase_token       text,
    purchase_type        integer,

    product_id           text,
    product_name         text,

    consumption_state    integer,

    country              text,
    currency             text,
    --Value after the actual price of a product is multiplied by 100.
    -- The actual price is accurate to two decimal places.
    -- For example, if the value of this parameter is 501, the actual product price is 5.01.
    price                integer,

    account_flag         integer,

    created              timestamp,
    updated              timestamp,
    primary key (id)
);

create table if not exists huawei_subscriptions
(
    id                            bigserial not null,
    android_package               text,
    --0: consumable
    --1: non-consumable
    --2: renewable subscription
    --3: non-renewable subscription
    kind                          integer   not null,

    order_id                      text,
    purchase_state                integer,

    price_amount_micros           bigint,
    price_currency_code           text,

    purchase_token                text,

    start_time_millis             timestamp,
    user_cancellation_time_millis timestamp,

    product_id                    text,
    product_name                  text,

    country                       text,
    --Value after the actual price of a product is multiplied by 100.
    -- The actual price is accurate to two decimal places.
    -- For example, if the value of this parameter is 501, the actual product price is 5.01.
    price                         integer,

    --subs fields
    --This parameter uniquely identifies the mapping between a product and a user. It does not change when the subscription is renewed
    subscription_id               text      not null,
    --ID of the subscription group to which a subscription belongs.
    product_group                 text      not null,

    sub_is_valid                  boolean,
    auto_renewing                 boolean,
    --can be filled if this subscription is switched from another
    ori_subscription_id           text,

    expiry_time_millis            timestamp,
    linked_purchase_token         text,

    account_flag                  integer,

    created                       timestamp,
    updated                       timestamp,
    primary key (id)
);


create table if not exists users_huawei_products
(
    huawei_product_id bigint not null,
    user_id           bigint not null,
    created           timestamp,
    updated           timestamp,
    primary key (huawei_product_id, user_id)
);

ALTER TABLE users_huawei_products
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table users_huawei_products
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);


create table if not exists users_huawei_subscriptions
(
    huawei_subscription_id bigint not null,
    user_id                bigint not null,
    created                timestamp,
    updated                timestamp,
    primary key (huawei_subscription_id, user_id)
);

ALTER TABLE users_huawei_subscriptions
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table users_huawei_subscriptions
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);
