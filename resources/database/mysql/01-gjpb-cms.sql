-- Use the new database
USE gjpb;

CREATE TABLE `cms_website` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(128) NOT NULL COMMENT 'Website name',
  `url` varchar(500) NOT NULL COMMENT 'Website URL (increased length for long URLs)',
  `logo_url` varchar(500) DEFAULT NULL COMMENT 'Logo image URL (increased length)',
  `description` varchar(1000) DEFAULT NULL COMMENT 'Website description (up to 1000 characters)',
  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search (e.g., Tech,Programming,Tutorial)',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the website content',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Order for display (lower = higher priority)',
  
  -- Audit Trail (following your project pattern)
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  
  -- Soft Delete
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',
  
  PRIMARY KEY (`id`),
  
  -- Indexes (optimized)
  UNIQUE KEY `uk_cms_website_name_lang` (`name`, `lang`), -- Unique constraint per language
  KEY `idx_cms_website_tags` (`tags`), -- Index for tag-based searches
  KEY `idx_cms_website_lang` (`lang`), -- Index for language-based queries
  KEY `idx_cms_website_display_order` (`display_order`),
  KEY `idx_cms_website_created_by` (`created_by`),
  KEY `idx_cms_website_updated_by` (`updated_by`),
  KEY `idx_cms_website_is_active` (`is_active`),
  KEY `idx_cms_website_active_order` (`is_active`, `display_order`), -- Composite index
  KEY `idx_cms_website_lang_active_order` (`lang`, `is_active`, `display_order`), -- Composite index for language queries
  
  -- Foreign Key Constraints (following your project pattern)
  CONSTRAINT `fk_cms_website_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_cms_website_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  
  -- Validation Constraints
  CONSTRAINT `chk_cms_website_url_format` CHECK (`url` LIKE 'http%'),
  CONSTRAINT `chk_cms_website_display_order` CHECK (`display_order` >= 0)
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Website links for display on the website';

-- ============================================================================
-- CMS Logo Table
-- Design: Frontend uploads any resolution → Backend resizes to 256
-- ============================================================================
CREATE TABLE `cms_logo` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(255) NOT NULL COMMENT 'Logo display name',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `extension` varchar(16) NOT NULL COMMENT 'File extension (png, jpg, webp, etc.)',
  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search (e.g., Tech,Programming,Tutorial)',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the website content',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Display order (lower = higher priority)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',
  
  PRIMARY KEY (`id`),
  
  -- Indexes
  UNIQUE KEY `uk_cms_logo_filename` (`filename`), -- Prevent duplicate files
  KEY `idx_cms_logo_name` (`name`),
  KEY `idx_cms_logo_extension` (`extension`),
  KEY `idx_cms_logo_created_by` (`created_by`),
  KEY `idx_cms_logo_updated_by` (`updated_by`),
  KEY `idx_cms_logo_is_active` (`is_active`),
  KEY `idx_cms_logo_lang` (`lang`),
  KEY `idx_cms_logo_active_order` (`is_active`, `display_order`), -- Composite index
  
  -- Foreign Key Constraints
  CONSTRAINT `fk_cms_logo_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_cms_logo_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  
  -- Validation Constraints
  CONSTRAINT `chk_cms_logo_display_order` CHECK (`display_order` >= 0),
  CONSTRAINT `chk_cms_logo_original_url` CHECK (`original_url` IS NULL OR `original_url` LIKE 'http%')
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Logo images';

CREATE TABLE `cms_image` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(255) NOT NULL COMMENT 'Image display name',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `source_name` varchar(255) DEFAULT NULL COMMENT 'Image source name',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `thumbnail_filename` varchar(255) DEFAULT NULL COMMENT 'Thumbnail filename',
  `extension` varchar(10) NOT NULL COMMENT 'File extension (png, jpg, webp, svg, etc.)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME type (image/jpeg, image/png, etc.)',
  `size_bytes` bigint UNSIGNED DEFAULT NULL COMMENT 'File size in bytes',
  `width` smallint UNSIGNED DEFAULT NULL COMMENT 'Image width in pixels',
  `height` smallint UNSIGNED DEFAULT NULL COMMENT 'Image height in pixels',
  `alt_text` varchar(500) DEFAULT NULL COMMENT 'Alt text for accessibility',
  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search (e.g., Tech,Programming,Tutorial)',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Content language',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Display order (lower = higher priority)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',

  PRIMARY KEY (`id`),
  INDEX `idx_active_lang_order` (`is_active`, `lang`, `display_order`),
  INDEX `idx_filename` (`filename`),
  INDEX `idx_thumbnail_filename` (`thumbnail_filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`),
  FULLTEXT INDEX `ftx_name_alt` (`name`, `alt_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMS Image Management';