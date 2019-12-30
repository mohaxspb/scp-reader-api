-- we need this to be able to select concrete number of pseudo-random rows from table.
-- example: `SELECT * FROM users TABLESAMPLE SYSTEM_ROWS (10);`
-- see https://www.postgresql.org/docs/current/tsm-system-rows.html
CREATE EXTENSION if not exists tsm_system_rows;