# ** *DEPRECATED in 2.17* **  Manual db prep SQL scripts
These scripts will prep an ohmage db by piping the list of files below to a database. As this method is now deprecated, please see the README in the `db/` directory or the `db/migration` directory for alternative options

```bash
sql/base/ohmage-ddl.sql
sql/preferences/default_preferences.sql
sql/settings/*.sql
```
If you're upgrading from an older ohmage release, you can use the update scripts found in `update/`, noting that multiple version skips will require you to execute each file in order (eg. ohmage 2.14 to ohmage 2.17 requires the execution of `2.14-2.15.sql`, `2.15-2.16.sql` and `2.16-2.17.sql`.