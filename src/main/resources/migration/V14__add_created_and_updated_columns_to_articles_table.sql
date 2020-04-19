alter table articles ADD COLUMN IF NOT EXISTS created timestamp;
alter table articles ADD COLUMN IF NOT EXISTS updated timestamp;