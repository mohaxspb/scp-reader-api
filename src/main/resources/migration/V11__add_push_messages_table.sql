create table if not exists push_messages
(
    id         BIGSERIAL not null,

    topic_name text,
    user_id    bigint,
    type       text      not null,
    title      text      not null,
    message    text      not null,
    url        text,

    author_id  bigint    not null,

    created    timestamp,
    updated    timestamp,
    PRIMARY KEY (id)
);


ALTER TABLE push_messages
    drop constraint IF EXISTS fk_user_id__to__users CASCADE;
alter table push_messages
    add constraint fk_user_id__to__users
        foreign key (user_id)
            REFERENCES users (id) on delete cascade;

ALTER TABLE push_messages
    drop constraint IF EXISTS fk_author_id__to__users CASCADE;
alter table push_messages
    add constraint fk_author_id__to__users
        foreign key (author_id)
            REFERENCES users (id);
