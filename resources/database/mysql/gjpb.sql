-- Drop existing databases if they exist
DROP DATABASE IF EXISTS gjpb;

-- Create new database
CREATE DATABASE gjpb CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Use the new database
USE gjpb;

-- create a table to store user login information
CREATE TABLE IF NOT EXISTS am_users (
    `id` char(36) NOT NULL,
    `username` varchar(50) NOT NULL,
    `password` varchar(100) NOT NULL,
    `display_order` int NOT NULL DEFAULT '0',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` varchar(255) DEFAULT NULL,
    `updated_by` varchar(255) DEFAULT NULL,
    `is_active` tinyint(1) NOT NULL DEFAULT '1' CHECK (`is_active` IN (0, 1)),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- create a table to store user roles
CREATE TABLE IF NOT EXISTS am_roles (
    `id` char(36) NOT NULL,
    `code` varchar(30) NOT NULL,
    `name` varchar(50) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `display_order` int NOT NULL DEFAULT '0',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` varchar(255) DEFAULT NULL,
    `updated_by` varchar(255) DEFAULT NULL,
    `is_active` tinyint(1) NOT NULL DEFAULT '1' CHECK (`is_active` IN (0, 1)),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_roles_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- create a table to store user permissions
CREATE TABLE IF NOT EXISTS am_user_roles (
    `user_id` char(36) NOT NULL,
    `role_id` char(36) NOT NULL,
    `display_order` int NOT NULL DEFAULT '0',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` varchar(255) DEFAULT NULL,
    `updated_by` varchar(255) DEFAULT NULL,
    `is_active` tinyint(1) NOT NULL DEFAULT '1' CHECK (`is_active` IN (0, 1)),
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_user_roles_user_id` (`user_id`),
    KEY `idx_user_roles_role_id` (`role_id`),
    CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `am_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES `am_roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert predefined roles
INSERT INTO am_roles (id, code, name, description, display_order, created_by, updated_by)
VALUES 
    ('5d51b31e-3756-11f0-a6c3-58d00cc8c893', 'ADMIN', 'System Administrator', 'Full system access with all privileges', 1, 'SYSTEM', 'SYSTEM'),
    ('5d51a824-3756-11f0-a6c3-58d00cc8c893', 'EDITOR', 'Content Editor', 'Create, edit, and publish all content', 2, 'SYSTEM', 'SYSTEM'),
    ('5d51aacc-3756-11f0-a6c3-58d00cc8c893', 'AUTHOR', 'Content Author', 'Create and edit own content', 3, 'SYSTEM', 'SYSTEM'),
    ('5d51ac0c-3756-11f0-a6c3-58d00cc8c893', 'MODERATOR', 'Content Moderator', 'Review and moderate user content', 4, 'SYSTEM', 'SYSTEM'),
    ('5d51b4c2-3756-11f0-a6c3-58d00cc8c893', 'USER', 'Regular User', 'Standard authenticated user privileges', 5, 'SYSTEM', 'SYSTEM'),
    ('5d51ad2e-3756-11f0-a6c3-58d00cc8c893', 'ANALYST', 'Data Analyst', 'Access to analytics and reporting features', 6, 'SYSTEM', 'SYSTEM'),
    ('5d51ae5a-3756-11f0-a6c3-58d00cc8c893', 'SUBSCRIBER', 'Premium Subscriber', 'Access to premium/paid content', 7, 'SYSTEM', 'SYSTEM'),
    ('5d51af72-3756-11f0-a6c3-58d00cc8c893', 'API_USER', 'API Integration User', 'External system integration access', 8, 'SYSTEM', 'SYSTEM'),
    ('5d51b1c0-3756-11f0-a6c3-58d00cc8c893', 'CONTENT_MANAGER', 'Content Manager', 'Manages content categories and organization', 9, 'SYSTEM', 'SYSTEM'),
    ('5d51b31k-3756-11f0-a6c3-58d00cc8c893', 'SUPPORT_AGENT', 'Support Agent', 'Customer/user support capabilities', 10, 'SYSTEM', 'SYSTEM');

-- Insert admin user 'admin' with BCrypt hashed password (password: 123456)
INSERT INTO am_users (id, username, password, created_by, updated_by)
VALUES ('a1b2c3d4-e5f6-11g0-h8i9-j0k1l2m3n4o5', 'admin', '$2a$10$Gi8uPjjsxdJWuzel/Ywiku3jjtgl.XeZE7LgW6a78sH7xJE/HileS', 'SYSTEM', 'SYSTEM');

-- Assign ADMIN role to user 'admin'
INSERT INTO am_user_roles (user_id, role_id, created_by, updated_by)
VALUES ('a1b2c3d4-e5f6-11g0-h8i9-j0k1l2m3n4o5', '5d51b31e-3756-11f0-a6c3-58d00cc8c893', 'SYSTEM', 'SYSTEM');
