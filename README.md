# Ohmage - Mobile Data Collection System

ohmage is a mobile data collection system for collecting data given explicitly
by a user (active data) and data that is collected by backgrounded applications
(passive data). This repository houses the server-side application.

## Working with Vagrant

This project includes a Vagrantfile to assist with setup of a working development
environment using Vagrant:

https://www.vagrantup.com/

You'll also need Virtualbox:

https://www.virtualbox.org/

The provided vagrant configuration will create an Ubuntu 12.04 virtual machine,
provisioning it with the necessary software via a shell script,
<code>dev-setup/bootstrap.sh</code>.  The Vagrantfile specifies ports to open
on the host machine for testing and debugging purposes.

Tomcat port 8080 is mapped to 4567.
Nginx port 80 is mapped to 4568.
The JVM remote debugging port 8000 is mapped to 5005.

Note that because the provisioning is done via shell script, it is not idempotent.
If you want to rerun it, perhaps after modification, the VM must be first destroyed,
then recreated.

After cloning the project you have the optional step to provide a dump of a mongo
test database.  Create a dump in /usr/share/vagrant-support/ohmage-test-db.  The
actual database will be a subdirectory in there named "ohmage".

This will allow the provisioning script to do a mongorestore from within the new VM, and the
replicated ohmage database will be up and running for you automatically.
