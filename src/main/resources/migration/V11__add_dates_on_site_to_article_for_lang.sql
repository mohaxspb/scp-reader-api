ALTER TABLE articles_langs ADD COLUMN IF NOT EXISTS created_on_site timestamp;
ALTER TABLE articles_langs ADD COLUMN IF NOT EXISTS updated_on_site timestamp;