# chmod 700 login.sh
# chmod 400 ~/.ssh/id_rsa

ansible-playbook ./playbook/setup_db_mysql.yml -i ~/.ansible/inventory/hosts -l ubuntu_server