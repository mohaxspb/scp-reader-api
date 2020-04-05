alter table users ADD COLUMN IF NOT EXISTS ads_disabled_end_date timestamp;
alter table users ADD COLUMN IF NOT EXISTS offline_limit_disabled_end_date timestamp;