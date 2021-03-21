create table if not exists users_to_push_tokens
(
    id                  bigserial not null,

    user_id             bigint    not null,
    push_token_value    text      not null,
    push_token_provider text      not null,

    created             timestamp,
    updated             timestamp,
    primary key (id)
);

ALTER TABLE users_to_push_tokens
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table users_to_push_tokens
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES scp_reader.public.users (id) ON DELETE CASCADE;

alter table users_to_push_tokens
    drop constraint if exists user_provider_token_unique;
alter table users_to_push_tokens
    add constraint user_provider_token_unique unique (user_id, push_token_provider, push_token_value);