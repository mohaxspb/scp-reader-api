create table if not exists huawei__oauth_access_token
(
    id           BIGSERIAL   not null,

    client_id    varchar(16) not null,
    access_token text        not null,
    expires_in   integer     not null,
    token_type    varchar(16) not null,

    created      timestamp,
    updated      timestamp,
    PRIMARY KEY (id)
);