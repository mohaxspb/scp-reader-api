create table if not exists user_data_transactions
(
    id               BIGSERIAL not null,
    user_id          bigint    not null,
    transaction_type text      not null,
    transaction_data text      not null default '',
    score_amount     integer   not null default 0,
    created          timestamp,
    updated          timestamp,
    PRIMARY KEY (id)
);

ALTER TABLE user_data_transactions
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table user_data_transactions
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id);
