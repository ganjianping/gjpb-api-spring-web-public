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

CREATE TABLE IF NOT EXISTS am_roles (
    `id` char(36) NOT NULL,
    `code` varchar(30) NOT NULL,
    `name` varchar(50) NOT NULL,
    `display_order` int NOT NULL DEFAULT '0',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` varchar(255) DEFAULT NULL,
    `updated_by` varchar(255) DEFAULT NULL,
    `is_active` tinyint(1) NOT NULL DEFAULT '1' CHECK (`is_active` IN (0, 1)),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_roles_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
    CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES am_users(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES am_roles(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;