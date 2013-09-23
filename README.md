# Welcome

ohmage is a mobile data collection system for collecting data given explicitly
by a user (active data) and data that is collected by backgrounded applications
(passive data). This repository houses the server-side application. The Android
application can be found at [here](https://github.com/ohmage/ohmageAndroidLib).

A description of the high-level entities can be found at [here](https://github.com/cens/ohmageServer/wiki/About-Users,-Classes-and-Campaigns),
and an introduction into how to read and write from an up-and-running system
can be found [here](https://github.com/cens/ohmageServer/wiki/About-the-Client-Server-Protocol-and-System-Entities).

This server is an Open mHealth DSU reference implementation for the [0.1 version](https://github.com/openmhealth/developer/wiki/DSU-API-0.1)
of the specification. The specification
has evolved but the implementation has not kept completely up-to-date. More
information can be found at the Open mHealth [Developer Wiki](https://github.com/openmhealth/developer/wiki).

# Setup

For web application veterans, all that is needed is a MySQL database and a
servlet container.

The default ohmage technology stack runs on various Linux distros and requires:
* Java 7
* MariaDB or MySQL 5.5
* Tomcat 7.0.26 (at the very minimum Tomcat 7.0.24 is required)
* nginx 1.4.2 or Apache httpd (used by the ohmage Linux installers)


## Setting Up the Database

ohmage depends on a MySQL instance. To update the database configuration,
update the appropriate `web/WEB-INF/spring/system.xml` options. The default is a
server on the local machine at the standard port of '3306' and a user named
'ohmage' with a password '&!sickly' that has full permissions on a database
named 'ohmage'.

In this database, several scripts will need to be run, which are all located
under the `db/sql/` directory. First must be `base/ohmage-ddl.sql`. Next must
be all of the files under `settings`. Finally, run the
`preferences/default_preferences.sql`.

## Setting Up the Directory Structure

ohmage depends on a set of directories to store log files and user data. These
are all located under `/opt/ohmage/`. The `log` directory will create iteself,
but the `userdata` directory will not. Under the `userdata` directory should
be three directories called `images`, `audio`, and `video`. These directories
can be changed through the `preference` table in the databse.

## Setting Up the Servlet Container

Any Servlet 3.0 compliant container should work. Internally, we use Tomcat. To
build the WAR file, there are two `ant` targets that should be run from the
root of the application. `dist` will build a WAR file that requires SSL, and
`dist-no_ssl` will build a WAR file that is SSL agnostic. The resulting WAR
file should then be copied to the servlet container's web applications
directory.

# Collaboration

The source is currently undergoing a major overhaul, so, unless a patch is
already in the works, it might be best to wait until the 3.0 version is
released.

The coding rules are loose, and the best reference would be other parts of the
code. A few rules we do have are:
- 4 space indents (no tabs).
- Always use curly braces even if the conditional or loop is one line.
- Opening curly braces go on the same line of the loop or conditional
declaration.
- All comments must be no more than 79 characters (with the 80th character
being a new line). Code should try to adhere to this as best as possible.
