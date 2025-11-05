# Ansible Configuration for gjpb-api-spring-web-public

This directory contains Ansible playbooks and configuration for deploying and managing the gjpb-api-spring-web-public application.

## 🔐 Security Notice

**This project uses a secure, user-account-based Ansible configuration.**

All sensitive data is stored in `~/.ansible/` (your home directory) and is encrypted using Ansible Vault. The configuration in this directory does NOT contain any credentials.

## 🚀 Quick Start

### First Time Setup

1. **Run the verification script:**
   ```bash
   ./verify_setup.sh
   ```

2. **Encrypt your vault (CRITICAL):**
   ```bash
   ./encrypt_vault.sh
   ```
   Or manually:
   ```bash
   ansible-vault encrypt ~/.ansible/inventory/group_vars/ubuntu_server/vault.yml
   ```

3. **Test the connection:**
   ```bash
   ansible ubuntu_server -m ping
   ```

### Deploy Application

```bash
ansible-playbook playbook/deploy.yml --ask-vault-pass
```

### Manage Database

```bash
# Create database
ansible-playbook playbook/create_db_mysql.yml --ask-vault-pass

# Setup database with tables and data
ansible-playbook playbook/setup_db_mysql.yml --ask-vault-pass
```

## 📁 Directory Structure

```
devops/ansible/
├── README.md                    # This file
├── SECURITY_SETUP.md           # Comprehensive security guide
├── SETUP_SUMMARY.md            # Quick setup summary
├── QUICK_REFERENCE.md          # Quick command reference
├── verify_setup.sh             # Verify Ansible setup
├── encrypt_vault.sh            # Helper to encrypt vault
├── playbook/
│   ├── deploy.yml              # Deploy Spring Boot application
│   ├── create_db_mysql.yml     # Create MySQL database
│   └── setup_db_mysql.yml      # Setup database tables and data
├── file/
│   └── mysql/                  # SQL scripts for database setup
└── inventory/
    └── hosts                   # DEPRECATED - Use ~/.ansible/inventory/hosts
```

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| `SECURITY_SETUP.md` | Complete security setup guide with all details |
| `SETUP_SUMMARY.md` | Quick summary of what was implemented |
| `QUICK_REFERENCE.md` | Quick reference for common commands |

## 🔧 Helper Scripts

| Script | Purpose |
|--------|---------|
| `verify_setup.sh` | Verify that Ansible is properly configured |
| `encrypt_vault.sh` | Helper to encrypt the vault file |

## 📍 Configuration Locations

All configuration is stored in your user account (`~/.ansible/`):

- **Main Config**: `~/.ansible/ansible.cfg`
- **Inventory**: `~/.ansible/inventory/hosts`
- **Variables**: `~/.ansible/inventory/group_vars/ubuntu_server/`
  - `vars.yml` - Non-sensitive variables
  - `vault.yml` - Encrypted sensitive credentials

## 🎯 Playbooks

### deploy.yml
Deploys the Spring Boot JAR file to the remote server.

**Variables used:**
- `jar_file_name` - Name of the JAR file (from vars.yml)
- `remote_project_path` - Remote deployment path (from vars.yml)
- `log_file_name` - Log file name (from vars.yml)

### create_db_mysql.yml
Creates MySQL database(s).

**Variables used:**
- `vault_db_user` - Database admin user (from vault.yml)
- `vault_db_pass` - Database admin password (from vault.yml)
- `databases` - List of databases to create (from vars.yml)

### setup_db_mysql.yml
Executes SQL scripts to create tables and insert data.

**Variables used:**
- `vault_db_user` - Database admin user (from vault.yml)
- `vault_db_pass` - Database admin password (from vault.yml)
- `db_name` - Database name (from vars.yml)
- `db_host` - Database host (from vars.yml)
- `sql_files` - List of SQL files to execute (from vars.yml)

## 🔐 Security Features

- ✅ **Ansible Vault Encryption** - Sensitive data is encrypted
- ✅ **Separate from Codebase** - Configuration stored in user account
- ✅ **Git-Safe** - No credentials in version control
- ✅ **Secure Permissions** - Files have restricted access
- ✅ **Best Practices** - Follows Ansible security guidelines

## ⚠️ Important Notes

1. **Never commit `~/.ansible/` to version control**
2. **Always use `--ask-vault-pass`** when running playbooks
3. **Keep your vault password secure** - store it in a password manager
4. **Backup your `~/.ansible/` directory** regularly
5. **Rotate credentials** periodically

## 🆘 Troubleshooting

### Vault password errors
Make sure you've encrypted the vault:
```bash
./encrypt_vault.sh
```

### Cannot find inventory
Verify `~/.ansible/ansible.cfg` exists and contains:
```ini
[defaults]
inventory = ~/.ansible/inventory/hosts
```

### Connection issues
Test SSH connection manually:
```bash
ssh -i ~/.ssh/id_rsa root@165.232.163.221
```

### More help
- Check the logs: `cat ~/.ansible/logs/ansible.log`
- Run with verbose output: `ansible-playbook playbook/deploy.yml -vvv --ask-vault-pass`
- See `SECURITY_SETUP.md` for detailed troubleshooting

## 📞 Support

For more information:
- See the comprehensive guide: `SECURITY_SETUP.md`
- Check quick reference: `QUICK_REFERENCE.md`
- Ansible Vault docs: https://docs.ansible.com/ansible/latest/user_guide/vault.html

## 🔄 Migrating from Old Setup

The old inventory file (`inventory/hosts`) has been deprecated. All playbooks now automatically use the configuration in `~/.ansible/`. No changes needed to your workflow - just add `--ask-vault-pass` when running playbooks.

---

**Ready to deploy? Start with `./verify_setup.sh` and `./encrypt_vault.sh`** 🚀
