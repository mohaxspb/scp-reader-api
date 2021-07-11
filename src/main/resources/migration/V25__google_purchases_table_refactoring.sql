drop TABLE users_android_subscriptions;


ALTER TABLE IF EXISTS android_subscriptions
    RENAME TO google_subscriptions;


create table if not exists user__to__google_subscriptions
(
    id                     bigserial not null,
    google_subscription_id bigint    not null,
    user_id                bigint    not null,
    created                timestamp,
    updated                timestamp,
    primary key (id)
);

ALTER TABLE user__to__google_subscriptions
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table user__to__google_subscriptions
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE user__to__google_subscriptions
    drop constraint IF EXISTS fk_google_subscription_id__to__google_subscriptions CASCADE;
alter table user__to__google_subscriptions
    add constraint fk_google_subscription_id__to__google_subscriptions
        foreign key (google_subscription_id)
            REFERENCES android_subscriptions (id) ON DELETE CASCADE;

alter table user__to__google_subscriptions
    drop constraint if exists google_subscription_id_and_user_id_unique;
alter table user__to__google_subscriptions
    add constraint google_subscription_id_and_user_id_unique unique (google_subscription_id, user_id);


create table if not exists google_subscription_event_handle_attempt
(
    id                  bigserial not null,

    decoded_data_json   text      not null,
    encodedData         text      not null,

    error_class         text,
    error_message       text,
    stacktrace          text,
    cause_error_class   text,
    cause_error_message text,
    cause_stacktrace    text,

    created             timestamp,
    updated             timestamp,
    primary key (id)
);