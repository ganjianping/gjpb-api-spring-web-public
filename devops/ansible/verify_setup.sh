#!/bin/bash
# Quick Setup Script for Ansible Security Configuration
# This script helps you verify and encrypt your Ansible vault

set -e

echo "=========================================="
echo "Ansible Security Setup Verification"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if files exist
echo "1. Checking file structure..."
if [ -f ~/.ansible/ansible.cfg ]; then
    echo -e "${GREEN}✓${NC} ansible.cfg exists"
else
    echo -e "${RED}✗${NC} ansible.cfg missing"
    exit 1
fi

if [ -f ~/.ansible/inventory/hosts ]; then
    echo -e "${GREEN}✓${NC} inventory/hosts exists"
else
    echo -e "${RED}✗${NC} inventory/hosts missing"
    exit 1
fi

if [ -f ~/.ansible/inventory/group_vars/ubuntu_server/vars.yml ]; then
    echo -e "${GREEN}✓${NC} group_vars/ubuntu_server/vars.yml exists"
else
    echo -e "${RED}✗${NC} group_vars/ubuntu_server/vars.yml missing"
    exit 1
fi

if [ -f ~/.ansible/inventory/group_vars/ubuntu_server/vault.yml ]; then
    echo -e "${GREEN}✓${NC} group_vars/ubuntu_server/vault.yml exists"
else
    echo -e "${RED}✗${NC} group_vars/ubuntu_server/vault.yml missing"
    exit 1
fi

echo ""
echo "2. Checking vault encryption status..."
if grep -q "\$ANSIBLE_VAULT" ~/.ansible/inventory/group_vars/ubuntu_server/vault.yml; then
    echo -e "${GREEN}✓${NC} vault.yml is encrypted"
    VAULT_ENCRYPTED=true
else
    echo -e "${YELLOW}⚠${NC} vault.yml is NOT encrypted"
    VAULT_ENCRYPTED=false
fi

echo ""
if [ "$VAULT_ENCRYPTED" = false ]; then
    echo "=========================================="
    echo "IMPORTANT: Vault is not encrypted!"
    echo "=========================================="
    echo ""
    read -p "Do you want to encrypt the vault now? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ansible-vault encrypt ~/.ansible/inventory/group_vars/ubuntu_server/vault.yml
        echo -e "${GREEN}✓${NC} Vault encrypted successfully"
        echo ""
        echo "REMEMBER YOUR VAULT PASSWORD!"
        echo "You'll need it to run playbooks with --ask-vault-pass"
    else
        echo -e "${YELLOW}⚠${NC} Skipping encryption. Remember to encrypt it later!"
    fi
fi

echo ""
echo "3. Verifying file permissions..."
chmod 600 ~/.ansible/inventory/group_vars/ubuntu_server/vault.yml
echo -e "${GREEN}✓${NC} vault.yml permissions set to 600"

if [ -f ~/.ansible/.vault_pass ]; then
    chmod 600 ~/.ansible/.vault_pass
    echo -e "${GREEN}✓${NC} .vault_pass permissions set to 600"
fi

echo ""
echo "=========================================="
echo "Setup verification complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Review SECURITY_SETUP.md for detailed documentation"
echo "2. Test connection: ansible ubuntu_server -m ping"
echo "3. Run playbooks with: ansible-playbook playbook/deploy.yml --ask-vault-pass"
echo ""
