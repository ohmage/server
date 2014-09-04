# Ohmage - Mobile Data Collection System

ohmage is a mobile data collection system for collecting data given explicitly
by a user (active data) and data that is collected by backgrounded applications
(passive data). This repository houses the server-side application.

## Working with Vagrant

This project includes a Vagrantfile to assist with setup of a working development
environment using Vagrant:

<https://www.vagrantup.com/>

You'll also need Virtualbox:

<https://www.virtualbox.org/>

The provided vagrant configuration will create an Ubuntu 12.04 virtual machine,
provisioning it with the necessary software via a shell script,
<code>dev-setup/bootstrap.sh</code>.  The Vagrantfile specifies ports to open
on the host machine for testing and debugging purposes.

| Port Description     | Port in VM | Port on Host Machine |
|----------------------|------------|----------------------|
| Tomcat               | 8080       | 4567                 |
| Nginx                | 80         | 4568                 |
| JVM Remote Debugging | 8000       | 5005                 |

Note that because the provisioning is done via shell script, it is not idempotent.
If you want to rerun it, perhaps after modification, the VM must be first destroyed,
then recreated.  The processes in the Vagrant VM will take a little while to start
up, so you likely won't be able to access EasyPost, mentioned below, right away.

## The Test Database

The provisioning script will automatically populate the mongo database with a test
ohmage database included in dev-setup/mongo.  This database will be populated with
three users with the following email addresses:

* ohmage-user-1@example.com
* ohmage-user-2@example.com
* ohmage-user-3@example.com

All have a password of "test", which allows you to easily get an authorization token.
There are three ohmlets, and each user owns one.  All ohmlets are public, so you can
see the definitions for all of them.  However, to see user information, you must be
logged in as that user.

* Ohmlet 1 - Contains no streams and one survey:
    * ohmlet_id: bb9afbe4-4102-4dd4-999d-7d357164a5d6
    * Owner: ohmage-user-3@example.com
    * Members: ohmage-user-1@example.com, ohmage-user-2@example.com

* Ohmlet 2 - Contains one stream and no surveys:
    * ohmlet_id: bcda9174-af7c-4ba7-8c59-a4766559864c
    * Owner: ohmage-user-2@example.com
    * Members: ohmage-user-3@example.com

* Ohmlet 3 - Contains one stream and one survey:
    * ohmlet_id: 8cafd868-6e6c-4bf9-9025-eb0e29506849
    * Owner: ohmage-user-1@example.com
    * Members: ohmage-user-2@example.com, ohmage-user-3@example.com

If you do not wish to use the test database, you can, after provisioning, drop the
ohmage database and start from scratch or use mongorestore to work with data from your
own ohmage database dump.

## EasyPost

EasyPost, after the web application server has fully started up, will be accessible on
port 4567 of the host machine.  You can refer to the wiki to learn how to
authenticate and read and write data to Ohmage using its web services API:

<https://github.com/ohmage/server/wiki/3.x-Documentation>