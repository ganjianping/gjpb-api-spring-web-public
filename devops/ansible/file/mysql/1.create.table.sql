-- Use the new database
USE gjpb;

-- Table: auth_users
-- Purpose: Store user accounts with authentication credentials, status, and security features
CREATE TABLE IF NOT EXISTS auth_users (
    id CHAR(36) NOT NULL COMMENT 'Primary Key (UUID)',
    nickname VARCHAR(30) DEFAULT NULL,

    -- Login credentials
    username       VARCHAR(30)   NOT NULL COMMENT 'Chosen username; must be ≥3 chars',
    email          VARCHAR(128)  NULL COMMENT 'Email address; validated by regex',
    mobile_country_code VARCHAR(5)  NULL COMMENT 'Country code, e.g. +65',
    mobile_number  VARCHAR(15)   NULL COMMENT 'Subscriber number, digits only',
    password_hash VARCHAR(128) NOT NULL COMMENT 'BCrypt or similar strong hash',

    -- Account Status Management
    account_status ENUM(
        'active',           -- User can log in
        'locked',           -- Temporarily locked (e.g., too many failed attempts)
        'suspended',        -- Administratively disabled
        'pending_verification' -- Awaiting email/SMS verification
    ) NOT NULL DEFAULT 'pending_verification',
    account_locked_until TIMESTAMP NULL DEFAULT NULL COMMENT 'Timestamp until which the account is locked',

    -- Login Tracking & Security
    last_login_at TIMESTAMP NULL DEFAULT NULL,
    last_login_ip VARCHAR(45) DEFAULT NULL COMMENT 'Last known IP address (IPv4/IPv6)',
    password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of last password change',
    failed_login_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Counter for consecutive failed login attempts',
    last_failed_login_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Timestamp of the last failed login attempt',

    -- Audit Trail
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36) DEFAULT NULL COMMENT 'UUID of the user who created this record',
    updated_by CHAR(36) DEFAULT NULL COMMENT 'UUID of the user who last updated this record',

    -- Soft Delete
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Logical deletion flag',

    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_users_username (username),
    UNIQUE KEY uk_auth_users_email (email),
    UNIQUE KEY uk_auth_users_phone (mobile_country_code, mobile_number),

    KEY idx_auth_users_account_status (account_status),
    KEY idx_auth_users_last_login (last_login_at),
    KEY idx_auth_users_created_by (created_by),
    KEY idx_auth_users_updated_by (updated_by),
    KEY idx_roles_active (is_active),

    CONSTRAINT fk_auth_users_created_by FOREIGN KEY (created_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_auth_users_updated_by FOREIGN KEY (updated_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT chk_auth_users_username_fmt CHECK (
        username REGEXP '^[A-Za-z0-9._-]{3,30}$'
    ),
    CONSTRAINT chk_users_email_fmt CHECK (
        email IS NULL OR email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'
    ),
    CONSTRAINT chk_auth_users_mobile_country_code_fmt CHECK (
        mobile_country_code IS NULL OR mobile_country_code REGEXP '^[1-9][0-9]{0,3}$'
    ),
    CONSTRAINT chk_auth_users_mobile_number_fmt CHECK (
        mobile_number IS NULL OR mobile_number REGEXP '^[0-9]{4,15}$'
    ),
    CONSTRAINT chk_contact_required CHECK (
        username REGEXP '^[A-Za-z0-9._-]{3,30}$'
        AND (
            email IS NULL OR email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
        )
        AND (
            mobile_country_code IS NULL
            OR (
                mobile_country_code IS NOT NULL
                AND mobile_number IS NOT NULL
                AND mobile_country_code REGEXP '^[1-9][0-9]{0,3}$'
                AND mobile_number REGEXP '^[0-9]{4,15}$'
            )
        )
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts with authentication credentials, status, and security features';

-- Table: auth_roles
-- Purpose: Define system roles with hierarchical structure support
CREATE TABLE IF NOT EXISTS auth_roles (
    id CHAR(36) NOT NULL COMMENT 'Primary Key (UUID)',
    code VARCHAR(50) NOT NULL COMMENT 'Unique role code (e.g., ADMIN, EDITOR)',
    name VARCHAR(100) NOT NULL COMMENT 'Human-readable role name',
    description TEXT DEFAULT NULL,
    parent_role_id CHAR(36) DEFAULT NULL COMMENT 'For hierarchical roles (parent role)',
    level INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Hierarchy level (0 = top level)',
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'TRUE if role is essential and cannot be deleted',
    sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Order for display or processing purposes',

    -- Audit Trail
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36) DEFAULT NULL,
    updated_by CHAR(36) DEFAULT NULL,

    -- Soft Delete / Activation
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'TRUE if the role can be assigned',

    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_roles_code (code),
    KEY idx_auth_roles_parent_id (parent_role_id),
    KEY idx_auth_roles_level (level),
    KEY idx_auth_roles_is_active (is_active),
    KEY idx_auth_roles_created_by (created_by),
    KEY idx_auth_roles_updated_by (updated_by),

    CONSTRAINT fk_auth_roles_parent FOREIGN KEY (parent_role_id) REFERENCES auth_roles (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_auth_roles_created_by FOREIGN KEY (created_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_auth_roles_updated_by FOREIGN KEY (updated_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_auth_roles_code_format CHECK (code REGEXP '^[A-Z][A-Z0-9_]*$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles with optional parent → child hierarchy';

-- Table: auth_user_roles
-- Purpose: Associate users with roles (many-to-many relationship)
CREATE TABLE IF NOT EXISTS auth_user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the role was granted',
    expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Optional: When the role grant expires',

    -- Audit Trail
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36) DEFAULT NULL,
    updated_by CHAR(36) DEFAULT NULL,

    -- Soft Delete / Activation
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'TRUE if this specific role assignment is active',

    PRIMARY KEY (user_id, role_id),
    KEY idx_auth_user_roles_user (user_id),
    KEY idx_auth_user_roles_role (role_id),
    KEY idx_auth_user_roles_active_expiry (is_active, expires_at), -- For finding current, valid roles
    KEY idx_auth_user_roles_created_by (created_by),
    KEY idx_auth_user_roles_updated_by (updated_by),

    CONSTRAINT fk_auth_user_roles_user FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_auth_user_roles_role FOREIGN KEY (role_id) REFERENCES auth_roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_auth_user_roles_created_by FOREIGN KEY (created_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_auth_user_roles_updated_by FOREIGN KEY (updated_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_auth_user_roles_expiry CHECK (expires_at IS NULL OR expires_at > granted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Links users to their assigned roles (M-M).';

-- Table: audit_logs

-- Purpose: Track all API operations for security and compliance
CREATE TABLE IF NOT EXISTS audit_logs (
    id CHAR(36) NOT NULL COMMENT 'Primary Key (UUID)',
    user_id CHAR(36) DEFAULT NULL COMMENT 'ID of user who performed the action',
    username VARCHAR(30) DEFAULT NULL COMMENT 'Username for quick reference',

    -- Request information
    http_method VARCHAR(10) NOT NULL COMMENT 'HTTP method (POST, PUT, PATCH, DELETE)',
    endpoint VARCHAR(255) NOT NULL COMMENT 'API endpoint that was called',
    request_id CHAR(36) DEFAULT NULL COMMENT 'Request ID from meta.requestId',

    -- Operation result (changed from ENUM to VARCHAR to store status.message)
    result VARCHAR(255) NOT NULL COMMENT 'Result message from response status.message',
    status_code INT DEFAULT NULL COMMENT 'HTTP status code',
    error_message TEXT DEFAULT NULL COMMENT 'Error details if operation failed',

    -- Client information
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'Client IP address (IPv4/IPv6)',
    user_agent TEXT DEFAULT NULL COMMENT 'Client user agent string',
    session_id VARCHAR(100) DEFAULT NULL COMMENT 'Session identifier',

    -- Performance tracking
    duration_ms BIGINT DEFAULT NULL COMMENT 'Operation duration in milliseconds',

    -- Timestamp
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the action occurred',

    PRIMARY KEY (id),

    -- Updated indexes for common queries
    KEY idx_audit_user_id (user_id),
    KEY idx_audit_timestamp (timestamp),
    KEY idx_audit_result (result),
    KEY idx_audit_endpoint (endpoint),
    KEY idx_audit_request_id (request_id),
    KEY idx_audit_user_timestamp (user_id, timestamp),
    KEY idx_audit_ip_address (ip_address),
    KEY idx_audit_status_code (status_code),

    -- Foreign key constraints
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Track all API operations for security and compliance';
USE gjpb;

-- Table: auth_refresh_tokens
-- Purpose: Store refresh tokens for JWT token rotation with secure hash storage
CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    id CHAR(36) NOT NULL COMMENT 'Primary Key (UUID)',
    user_id CHAR(36) NOT NULL COMMENT 'Foreign key to auth_users.id',
    token_hash VARCHAR(255) NOT NULL COMMENT 'SHA-256 hash of the refresh token for secure storage',
    expires_at TIMESTAMP NOT NULL COMMENT 'Expiration timestamp of the refresh token',
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether this token has been manually revoked',
    revoked_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Timestamp when the token was revoked',
    last_used_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Timestamp when this token was last used for refresh',
    
    -- Audit Trail
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by CHAR(36) DEFAULT NULL COMMENT 'UUID of the user who created this record',
    updated_by CHAR(36) DEFAULT NULL COMMENT 'UUID of the user who last updated this record',

    PRIMARY KEY (id),
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_hash (token_hash),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    INDEX idx_refresh_tokens_user_valid (user_id, expires_at, is_revoked),
    
    CONSTRAINT fk_refresh_tokens_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES auth_users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) 
ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci 
COMMENT='Refresh tokens for JWT authentication with token rotation support';

-- Create index for efficient cleanup of expired tokens
CREATE INDEX idx_refresh_tokens_cleanup ON auth_refresh_tokens (expires_at, is_revoked);

CREATE TABLE IF NOT EXISTS bm_app_settings (
    id CHAR(36) NOT NULL COMMENT 'Primary Key (UUID)',
    name VARCHAR(50) NOT NULL COMMENT 'Setting name (unique identifier)',
    value VARCHAR(500) DEFAULT NULL COMMENT 'Setting value',
    
    -- Internationalization support
    lang ENUM('EN', 'ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the setting',

    -- Configuration properties
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'System config (not user editable)',
    is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Public config (visible to non-admin users)',
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    created_by CHAR(36) DEFAULT NULL COMMENT 'Created by user ID',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    updated_by CHAR(36) DEFAULT NULL COMMENT 'Last updated by user ID',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_bm_app_settings_name_lang (name, lang),
    KEY idx_system_configs_is_public (is_public),
    KEY idx_system_configs_is_system (is_system),
    KEY idx_system_configs_created_by (created_by),
    KEY idx_system_configs_updated_by (updated_by),
    
    -- Foreign key constraints
    CONSTRAINT fk_system_configs_created_by FOREIGN KEY (created_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_system_configs_updated_by FOREIGN KEY (updated_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Application settings with internationalization support';

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

CREATE TABLE `cms_video` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(255) NOT NULL COMMENT 'Video display name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Video description',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `size_bytes` bigint UNSIGNED DEFAULT NULL COMMENT 'File size in bytes',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `source_name` varchar(255) DEFAULT NULL COMMENT 'Video source name',
  `cover_image_filename` varchar(500) DEFAULT NULL COMMENT 'Cover image filename (stored in uploads)',
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
  INDEX `idx_original_url` (`original_url`),
  INDEX `idx_source_name` (`source_name`),
  INDEX `idx_tags` (`tags`),
  INDEX `idx_cover_image_filename` (`cover_image_filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CMS Video Management';

CREATE TABLE `cms_audio` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(255) NOT NULL COMMENT 'Audio display name',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `source_name` varchar(255) DEFAULT NULL COMMENT 'Audio source name',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `size_bytes` bigint UNSIGNED DEFAULT NULL COMMENT 'File size in bytes',
  `cover_image_filename` varchar(500) DEFAULT NULL COMMENT 'Cover image filename (stored in uploads)',
  `description` varchar(500) DEFAULT NULL COMMENT 'Audio description',
  `subtitle` text DEFAULT NULL COMMENT 'Subtitle or transcript',
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
  INDEX `idx_original_url` (`original_url`),
  INDEX `idx_source_name` (`source_name`),
  INDEX `idx_filename` (`filename`),
  INDEX `idx_cover_image_filename` (`cover_image_filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CMS Audio Management';


CREATE TABLE `cms_article` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `title` varchar(255) NOT NULL COMMENT 'Article title',
  `summary` varchar(500) DEFAULT NULL COMMENT 'Brief article summary/excerpt',
  `content` longtext DEFAULT NULL COMMENT 'Full article content',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `source_name` varchar(255) DEFAULT NULL COMMENT 'Content source name',
  `cover_image_filename` varchar(500) DEFAULT NULL COMMENT 'Cover image file path',
  `cover_image_original_url` varchar(500) DEFAULT NULL COMMENT 'Cover image original URL',
  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search (e.g., Tech,Programming,Tutorial)',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Content language',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Display order (lower = higher priority)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',

   PRIMARY KEY (`id`),
   UNIQUE KEY `idx_title_lang` (`title`, `lang`),
   KEY `idx_tags` (`tags`),
   KEY `idx_original_url` (`original_url`),
   KEY `idx_lang_active` (`lang`, `is_active`),
   KEY `idx_display_order` (`display_order`),
   KEY `idx_created_at` (`created_at`),
   KEY `idx_created_by` (`created_by`),
   KEY `idx_updated_by` (`updated_by`),
   KEY `idx_is_active` (`is_active`),
   KEY `idx_active_order` (`is_active`, `display_order`),
   KEY `idx_lang_active_order` (`lang`, `is_active`, `display_order`),
   FULLTEXT KEY `idx_fulltext_search` (`title`, `summary`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CMS Article Management';

CREATE TABLE `cms_file` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(255) NOT NULL COMMENT 'File display name',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `source_name` varchar(255) DEFAULT NULL COMMENT 'File source name',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `size_bytes` bigint UNSIGNED DEFAULT NULL COMMENT 'File size in bytes',
  `extension` varchar(16) NOT NULL COMMENT 'File extension (pdf, docx, xlsx, etc.)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME type (application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, etc.)',
  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search (e.g., Tech,Programming,Tutorial)',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Content language',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Display order (lower = higher priority)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',
  PRIMARY KEY (`id`),
  Unique KEY `uk_cms_file_filename` (`filename`), -- Prevent duplicate files
  INDEX `idx_active_lang_order` (`is_active`, `lang`, `display_order`),
  INDEX `idx_filename` (`filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMS File Management';
