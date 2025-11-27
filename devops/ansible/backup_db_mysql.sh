#!/bin/bash
# Run the backup_db_mysql playbook against the inventory group 'ubuntu_server'.
# Usage: ./backup_db_mysql.sh
# This script relies on your user Ansible inventory at ~/.ansible/inventory/hosts
# You can override inventory with -i or set ANSIBLE_CONFIG if needed.
ansible-playbook ./playbook/backup_db_mysql.yml -i ~/.ansible/inventory/hosts -l ubuntu_server
