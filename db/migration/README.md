# First Class Migrations!
Happy Day! Migrations have finally arrived for ohmage.  Thanks to the awesome [flyway](https://flywaydb.org), this directory has the hook up to easy db prepping and upgrades!

## Execution
After following the download/config procedures below, you can simply run `flyway migrate` to run new migrations. Please read the note on baselining below if you have an existing ohmage database that you'd like to get moving with flyway.

## Flyway download
Assuming you're in your home directory, follow the commands below to download/extract flyway:
```bash
wget http://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/3.2.1/flyway-commandline-3.2.1.tar.gz
tar xvf flyway-commandline-3.2.1.tar.gz
```

You'll now have a directory at `~/flyway-3.2.1/`. If you want the flyway executable in your path, go for it: `export PATH=~/flyway-3.2.1/:$PATH` or just `cd` into the directory and execute `./flyway` as needed.

## Config
Assuming you've followed the above, the default flyway config file lives at `~/flyway-3.2.1/conf/flyway.conf`. When you execute flyway you can pass `-configFile=/path/to/your/alt/location` to specify a different location. The configuration is quite simple, an example below (remember to replace the $VARIABLES with your own db connection info!).

```
flyway.url=jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME
flyway.user=$DB_USER
flyway.password=$DB_PASSWORD
flyway.placeholders.fqdn=$FQDN
flyway.placeholders.base_dir=/var/lib/ohmage
flyway.locations=filesystem:/path/to/this/migration/directory
```
`fqdn` and `base_dir` placeholders are ohmage specific and should be set to the fully qualified domain name of the ohmage server as well as the base directory for filesystem storage of ohmage survey response media. Additionally, you don't need to set the "locations" parameter if drop the migrations in this directory to `~/flyway-3.2.1/sql`.

## A note on "baselining"
If you're starting with an existing ohmage 2.16 database, please execute `flyway baseline -baselineVersion=3` to set flyway migrations to the baseline of ohmage 2.16. 