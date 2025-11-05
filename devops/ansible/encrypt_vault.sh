#!/bin/bash
# Encrypt Ansible Vault - Helper Script

set -e

echo "=========================================="
echo "Ansible Vault Encryption Helper"
echo "=========================================="
echo ""

VAULT_FILE=~/.ansible/inventory/group_vars/ubuntu_server/vault.yml

# Check if file exists
if [ ! -f "$VAULT_FILE" ]; then
    echo "❌ Error: Vault file not found at $VAULT_FILE"
    exit 1
fi

# Check if already encrypted
if grep -q "\$ANSIBLE_VAULT" "$VAULT_FILE"; then
    echo "✓ Vault is already encrypted"
    echo ""
    echo "Available options:"
    echo "  1. View vault contents:  ansible-vault view $VAULT_FILE"
    echo "  2. Edit vault contents:  ansible-vault edit $VAULT_FILE"
    echo "  3. Change password:      ansible-vault rekey $VAULT_FILE"
    echo "  4. Decrypt vault:        ansible-vault decrypt $VAULT_FILE"
    exit 0
fi

echo "⚠️  WARNING: Your vault file is NOT encrypted!"
echo ""
echo "The vault file contains sensitive credentials:"
echo "  - Database username and password"
echo "  - Other sensitive configuration"
echo ""
echo "It is HIGHLY RECOMMENDED to encrypt it now."
echo ""
read -p "Do you want to encrypt the vault now? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "You will be prompted to create a vault password."
    echo "IMPORTANT: Remember this password! You'll need it to run playbooks."
    echo ""
    echo "Encrypting vault..."
    
    ansible-vault encrypt "$VAULT_FILE"
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ SUCCESS! Vault has been encrypted."
        echo ""
        echo "Next steps:"
        echo "  1. Test connection:  ansible ubuntu_server -m ping"
        echo "  2. Run a playbook:   ansible-playbook playbook/deploy.yml --ask-vault-pass"
        echo ""
        echo "To edit the vault in the future:"
        echo "  ansible-vault edit $VAULT_FILE"
        echo ""
    else
        echo ""
        echo "❌ Encryption failed. Please check the error message above."
        exit 1
    fi
else
    echo ""
    echo "⚠️  Skipping encryption."
    echo ""
    echo "To encrypt later, run:"
    echo "  ansible-vault encrypt $VAULT_FILE"
    echo ""
    echo "Or run this script again:"
    echo "  ./encrypt_vault.sh"
    echo ""
fi
