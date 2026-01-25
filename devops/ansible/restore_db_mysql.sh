#!/bin/bash
# Run the restore_db_mysql playbook against the inventory group 'ubuntu_server'.
# Usage: ./restore_db_mysql.sh
ansible-playbook ./playbook/restore_db_mysql.yml -i ~/.ansible/inventory/hosts -l ubuntu_server
