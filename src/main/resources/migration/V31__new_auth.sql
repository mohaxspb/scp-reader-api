-- missed columns, which we might not need
--     attributes blob DEFAULT NULL,
--     access_token_metadata blob DEFAULT NULL,
--     refresh_token_metadata blob DEFAULT NULL,

create table if not exists oauth_authorization_new
(
    id                       bigserial not null,

    registered_client_id     text      not null,
    principal_name           text,
    authorization_grant_type text      not null,

    access_token_value       text      DEFAULT NULL,
    access_token_issued_at   timestamp DEFAULT NULL,
    access_token_expires_at  timestamp DEFAULT NULL,
    access_token_scopes      text      DEFAULT NULL,

    refresh_token_value      text      DEFAULT NULL,
    refresh_token_issued_at  timestamp DEFAULT NULL,
    refresh_token_expires_at timestamp DEFAULT NULL,

    primary key (id)
);

ALTER TABLE oauth_authorization_new
    drop constraint IF EXISTS oauth_authorization_new_principal_name__to_users CASCADE;
alter table oauth_authorization_new
    add constraint oauth_authorization_new_principal_name__to_users
        foreign key (principal_name)
            REFERENCES users (username) ON DELETE CASCADE;

ALTER TABLE oauth_authorization_new
    drop constraint IF EXISTS oauth_new_registered_client_id__to_oauth_client_details CASCADE;
alter table oauth_authorization_new
    add constraint oauth_new_registered_client_id__to_oauth_client_details
        foreign key (registered_client_id)
            REFERENCES oauth_client_details (client_id) ON DELETE CASCADE;

