#!/bin/bash
# Run the backup_resources playbook against the inventory group 'ubuntu_server'.
# Usage: ./backup_resources.sh
ansible-playbook ./playbook/backup_resources.yml -i ~/.ansible/inventory/hosts -l ubuntu_server
