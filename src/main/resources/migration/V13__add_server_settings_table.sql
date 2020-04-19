create table if not exists server_settings
(
    id        BIGSERIAL not null,

    key       text      not null,
    value     text      not null,

    author_id bigint    not null,

    created   timestamp,
    updated   timestamp,
    PRIMARY KEY (id)
);

alter table server_settings
    drop constraint if exists key_unique;
alter table server_settings
    add constraint key_unique unique (key);

ALTER TABLE server_settings
    drop constraint IF EXISTS fk_author_id__to__users CASCADE;
alter table server_settings
    add constraint fk_author_id__to__users
        foreign key (author_id)
            REFERENCES users (id);
