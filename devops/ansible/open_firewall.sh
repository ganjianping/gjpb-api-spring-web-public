# chmod 700 login.sh
# chmod 400 ~/.ssh/id_rsa

ansible-playbook ./playbook/open_firewall.yml -i ~/.ansible/inventory/hosts -l ubuntu_server