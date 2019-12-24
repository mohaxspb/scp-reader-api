create table if not exists article_parse_errors
(
    id                  BIGSERIAL              not null,
    lang_id             character varying(255) not null,
    url_relative        text                   not null,
    error_class         text                   not null,
    error_message       text,
    stacktrace          text                   not null,
    cause_error_class   text,
    cause_error_message text,
    cause_stacktrace    text,
    created             timestamp,
    updated             timestamp,
    PRIMARY KEY (id)
);

ALTER TABLE article_parse_errors
    drop constraint IF EXISTS fk_lang_id__to__langs CASCADE;
alter table article_parse_errors
    add constraint fk_lang_id__to__langs
        foreign key (lang_id)
            REFERENCES langs (id);
