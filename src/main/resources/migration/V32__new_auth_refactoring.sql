-- delete duplicated rows

delete
from oauth_authorization_new o1 using oauth_authorization_new o2
where o1.principal_name=o2.principal_name AND o1.id<o2.id;

-- add unique constraints

ALTER TABLE oauth_authorization_new
    drop constraint IF EXISTS oauth_authorization_new__unique__principal_name CASCADE;
alter table oauth_authorization_new
    add constraint oauth_authorization_new_unique
      unique (principal_name);

ALTER TABLE oauth_authorization_new
    drop constraint IF EXISTS oauth_authorization_new__unique__access_token_value CASCADE;
alter table oauth_authorization_new
    add constraint oauth_authorization_new__unique__access_token_value
      unique (access_token_value);

ALTER TABLE oauth_authorization_new
    drop constraint IF EXISTS oauth_authorization_new__unique__refresh_token_value CASCADE;
alter table oauth_authorization_new
    add constraint oauth_authorization_new__unique__refresh_token_value
      unique (refresh_token_value);