create table if not exists search_stats
(
    id              bigserial not null,

    lang_id         varchar   not null,
    query           text      not null,
    num_of_requests bigint    not null default 0,

    primary key (id)
);

ALTER TABLE search_stats
    drop constraint IF EXISTS search_stats__to__langs CASCADE;
alter table search_stats
    add constraint search_stats__to__langs
        foreign key (lang_id)
            REFERENCES langs (id) ON DELETE CASCADE;

alter table search_stats
    drop constraint if exists search_stats__lang_id__and__query__unique;
alter table search_stats
    add constraint search_stats__lang_id__and__query__unique unique (lang_id, query);