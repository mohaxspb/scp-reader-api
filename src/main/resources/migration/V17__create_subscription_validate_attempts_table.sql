create table if not exists subscription_validate_attempts
(
    id                bigserial not null,

    subscription_id   bigint    not null,
    store             text      not null,

    attempts          integer   not null,
    last_attempt_time timestamp not null,

    created           timestamp,
    updated           timestamp,
    primary key (id)
);

alter table subscription_validate_attempts
    drop constraint if exists subscription_id_and_store_unique;
alter table subscription_validate_attempts
    add constraint subscription_id_and_store_unique unique (subscription_id, store);