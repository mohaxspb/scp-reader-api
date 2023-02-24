### DB backup restore command

1. Start `psql`

```shell
psql -U postgres
```

2. Connect to DB

```shell
\c scp_reader
```

3. Enter restore script

```shell
\i /path/to/filename.sql
```