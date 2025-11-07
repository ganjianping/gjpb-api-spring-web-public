# chmod 700 login.sh
# chmod 400 ~/.ssh/id_rsa

ansible-playbook ./playbook/create_upload_folders.yml -i ~/.ansible/inventory/hosts -l ubuntu_server