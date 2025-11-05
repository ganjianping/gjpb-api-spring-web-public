#!/bin/bash
# Build and deploy gjpb-api-spring-web-public to ubuntu_server
# Usage: ./deploy.sh

set -e  # Exit on error

echo "=========================================="
echo "Building gjpb-api-spring-web-public..."
echo "=========================================="

# Navigate to project root (two levels up from devops/ansible)
cd "$(dirname "$0")/../.."

# Build the project using Maven wrapper (skip tests for faster builds)
./mvnw clean package -DskipTests

echo ""
echo "=========================================="
echo "Build complete. Deploying to ubuntu_server..."
echo "=========================================="

# Return to ansible directory
cd devops/ansible

# Run the deployment playbook
ansible-playbook ./playbook/deploy.yml -i ~/.ansible/inventory/hosts -l ubuntu_server

echo ""
echo "=========================================="
echo "Deployment complete!"
echo "=========================================="