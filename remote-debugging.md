To remote-debug with IntelliJ
=============================

#### Create debug runtime configuration
  1. Run -> Edit Configurations
  2. Click the +
  3. Select "Remote"
  4. Set the name to "Debug in Vagrant"
  5. Accept the defaults, for localhost port 5005
  6. Click OK
  
  Make sure Virtualbox is installed:  https://www.virtualbox.org/

  Install Vagrant IntelliJ Plugin:  
  Preferences -> Plugins -> Install JetBrains plugin -> Type Vagrant

  Restart IntelliJ
  
#### Startup Vagrant
  1. Tools -> Vagrant -> Destroy
  2. Tools -> Vagrant -> Up
  3. Wait for provisioning of the virtual machine to complete

  Run -> Debug... -> Debug in Vagrant