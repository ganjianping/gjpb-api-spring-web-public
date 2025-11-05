# chmod 700 login.sh
# chmod 400 ~/.ssh/ubuntu_rsa

#!/bin/bash
# Run the create_db_mysql playbook against the inventory group 'ubuntu_server'.
# Usage: ./create_db_mysql.sh
# This script relies on your user Ansible inventory at ~/.ansible/inventory/hosts
# You can override inventory with -i or set ANSIBLE_CONFIG if needed.

# Preferred: specify the playbook first, then options. Use -l to limit to group
ansible-playbook ./playbook/create_db_mysql.yml -i ~/.ansible/inventory/hosts -l ubuntu_server