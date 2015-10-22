# Setting up the ohmage database
*This process is changing, starting with the 2.17 release!*

## Migrations
Starting with ohmage 2.17, the ohmage db setup is getting a (majorly needed) overhaul.  Most importantly, you should notice the new directory, `/migration` which contain the needed db migration scripts. These migrations are supported via [Flyway](http://flywaydb.org/).  Take a look at the README in that directory for information on execution, but some very brief info is below:
### Updating from 2.16 to 2.17
Since the flyway migrations begin to exist with 2.17, we'll first need to baseline the existing db.  With the flyway executable in your path, and it's config given the proper db info (take a look at the README in the `migration` directory for more help on this) execute:
```bash
flyway baseline -baselineVersion=3
```
This will **not** touch your existing 2.16 database, save for creating a new table which will manage the migrations and marking all migrations up to V3 as "successful".  From here on, migrations can be done by simply executing `flyway migrate`. This is safe to execute as many times as you'd like, it will only perform new migrations.
### "From scratch"
With the flyway dependencies resolved/set up, you can simply execute `flyway migrate` to get started.  It should be noted that one of the migrations (V3) is actually seed data (pertaining to filesystem specifics, terms of service, etc.) that you may want to modify before executing depending on your environment.  The defaults, however, are quite sane and can always be changed in the `preference` table at a later time.  Again, please take a look at the README file in the migrations directory if you have more questions about the process. 

## Manual SQL scripts
*Please note that the 2.17 version of the server code will be the last version to offer this SQL script option*. Additionally, these scripts can only be run once, and do not guarantee success/failure outputs.
### Updating from 2.16 to 2.17
Please execute the script found at `sql/update/2.16-2.17.sql` to prepare your database for the ohmage 2.17 upgrade.
### "From scratch"
In order to get a working ohmage 2.17 database from scratch, you'll need to pipe a number of files into the ohmage db:
```bash
sql/base/ohmage-ddl.sql
sql/preferences/default_preferences.sql
sql/settings/*.sql
```