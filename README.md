# ohmage API server

[![Join the chat at https://gitter.im/ohmage/server](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ohmage/server?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

ohmage is a mobile data collection system for collecting data given explicitly by a user (active data) and data that is collected by backgrounded applications (passive data). This repository houses the server-side application. The Android application can be found at [here](https://github.com/ohmage/ohmageAndroidLib).

A description of the high-level entities can be found at [here](https://github.com/cens/ohmageServer/wiki/About-Users,-Classes-and-Campaigns), and an introduction into how to read and write from an up-and-running system can be found [here](https://github.com/cens/ohmageServer/wiki/About-the-Client-Server-Protocol-and-System-Entities).

The API docs for the server can be found [here](https://github.com/ohmage/server/wiki/APIs-for-2.x-Top-Level-Entities)

# Setup

For web application veterans, all that is needed is a MySQL database and a servlet container.

The default ohmage technology stack runs on various Linux distros and requires:
* Java 7
* MariaDB 5.5 or MySQL 5.5
* Tomcat 7.0.28 or later. 

For internal hosting and development, the ohmage team uses nginx 1.9 for 
serving up static content and better SSL performance.

## Setting Up the Database

ohmage depends on a MySQL instance. To set the database configuration, create an `/etc/ohmage.conf` file with the following parameters (fill in host,port,db name, user and password as needed, the defaults are shown):
```
db.driver=com.mysql.jdbc.Driver
db.jdbcurl=jdbc:mysql://127.0.0.1:3306/ohmage?characterEncoding=utf8
db.username=ohmage
db.password=&!sickly
```

Please see the `db/migrations` dir for more information on schema creation and migrating the database.

## Setting Up the Directory Structure

ohmage depends on a set of directories to store log files and user data. By default, these are located at `/var/lib/ohmage/`. This directory should contain a number of subdirectories, called `audits`, `audio`, `images`, `documents` and `videos`. These directories can be changed through the `preference` table in the database. The `log` directory (default: `/var/log/ohmage`) will create itself.

## Setting Up the Servlet Container

Any Servlet 3.0 compliant container should work. Internally, we use Tomcat. To build the WAR file, use `ant clean dist`, which will produce an ssl-disabled container. It should be noted that we do not recommend having the servlet itself handle SSL, and instead suggest you use a web server like nginx or apache to do SSL termination.

# Collaboration

The coding rules are loose, and the best reference would be other parts of the code. A few rules we do have are:
  * 4 space indents (no tabs).
  * Always use curly braces even if the conditional or loop is one line.
  * Opening curly braces go on the same line of the loop or conditional declaration.
  * All comments must be no more than 79 characters (with the 80th character being a new line). Code should try to adhere to this as best as possible.
