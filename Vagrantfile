# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.provider "virtualbox" do |v|
    v.memory = 2048
  end
  config.vm.box = "hashicorp/precise64"
  config.vm.synced_folder "/usr/share/vagrant-support/", "/vagrant/vagrant-support"
  config.vm.provision :shell, path: "dev-setup/bootstrap.sh"
  #config.vm.provision "ansible" do |ansible|
  #  ansible.playbook = "dev-setup/playbook.yml"
  #end
  config.vm.network :forwarded_port, host: 4567, guest: 8080
  config.vm.network :forwarded_port, host: 4568, guest: 80
  config.vm.network :forwarded_port, host: 5005, guest: 8000
end
