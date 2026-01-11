-- Use the new database
USE gjpb;

CREATE TABLE `rubi_vocabulary` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `word` varchar(100) NOT NULL COMMENT 'The vocabulary word',
  `phonetic` varchar(100) DEFAULT NULL COMMENT 'Phonetic transcription',

  `part_of_speech` varchar(50) DEFAULT NULL COMMENT 'Part of speech (noun, verb, etc.)',
  `simple_past_tense` varchar(100) DEFAULT NULL COMMENT 'Past tense form (for verbs)',
  `past_perfect_tense` varchar(100) DEFAULT NULL COMMENT 'Past perfect tense form (for verbs)',
  `plural_form` varchar(100) DEFAULT NULL COMMENT 'Plural form (for nouns)',
  `translation` varchar(500) DEFAULT NULL COMMENT 'translation of the word',
  `synonyms` varchar(200) DEFAULT NULL COMMENT 'Comma-separated synonyms',
  `definition` varchar(2000) DEFAULT NULL COMMENT 'Definition of the word',
  `example` varchar(2000) DEFAULT NULL COMMENT 'Example sentence using the word',
  `dictionary_url` varchar(500) DEFAULT NULL COMMENT 'Link to an online dictionary entry',
  `word_image_filename` varchar(100) DEFAULT NULL COMMENT 'Word image file path',
  `word_image_original_url` varchar(500) DEFAULT NULL COMMENT 'Word image original URL',
  `phonetic_audio_filename` varchar(100) DEFAULT NULL COMMENT 'Phonetic audio file path',
  `phonetic_audio_original_url` varchar(500) DEFAULT NULL COMMENT 'Phonetic audio original URL',
  
  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search (e.g., Tech,Programming,Tutorial)',
  `difficulty_level` varchar(20) DEFAULT NULL COMMENT 'Difficulty level of the vocabulary',
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
  UNIQUE KEY `uniq_rubi_vocab_word_lang` (`word`, `lang`),

  -- Indexes
  KEY `idx_rubi_vocab_word` (`word`),
  KEY `idx_rubi_vocab_tags` (`tags`),
  KEY `idx_rubi_vocab_lang` (`lang`),
  KEY `idx_rubi_vocab_is_active` (`is_active`),
  
  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_vocab_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_vocab_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vocabulary words for language learning';

-- Multiple Choice Questions Table
CREATE TABLE `rubi_multiple_choice_question` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `question` varchar(500) NOT NULL COMMENT 'The question text',
  `option_a` varchar(200) DEFAULT NULL COMMENT 'Option A',
  `option_b` varchar(200) DEFAULT NULL COMMENT 'Option B',
  `option_c` varchar(200) DEFAULT NULL COMMENT 'Option C',
  `option_d` varchar(200) DEFAULT NULL COMMENT 'Option D',
  `answer` varchar(10) NOT NULL COMMENT 'Comma-separated correct answer options (e.g., A,C)',
  `explanation` varchar(1000) DEFAULT NULL COMMENT 'Explanation for the correct answer',
  `difficulty_level` varchar(20) DEFAULT NULL COMMENT 'Difficulty level of the question',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT 'Number of times users answered incorrectly',
  `success_count` int NOT NULL DEFAULT '0' COMMENT 'Number of times users answered correctly',

  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the question content',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Order for display (lower = higher priority)',
  
  -- Audit Trail
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  
  -- Soft Delete
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rubi_mcq_question_lang` (`question`, `lang`),
  
  -- Indexes
  KEY `idx_rubi_mcq_difficulty` (`difficulty_level`),
  KEY `idx_rubi_mcq_tags` (`tags`),
  KEY `idx_rubi_mcq_lang` (`lang`),
  KEY `idx_rubi_mcq_is_active` (`is_active`),
  
  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_mcq_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_mcq_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multiple choice questions for vocabulary practice';

-- Free Text Answer Questions Table
CREATE TABLE `rubi_free_text_question` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `question` varchar(500) NOT NULL COMMENT 'The question text',
  `answer` varchar(1000) NOT NULL COMMENT 'The correct answer',
  `explanation` varchar(2000) DEFAULT NULL COMMENT 'Explanation for the correct answer',  
  `difficulty_level` varchar(20) DEFAULT NULL COMMENT 'Difficulty level of the question',

  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the question content',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Order for display (lower = higher priority)',
  
  -- Audit Trail
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  
  -- Soft Delete
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rubi_ftq_question_lang` (`question`, `lang`),
  
  -- Indexes
  KEY `idx_rubi_ftq_difficulty` (`difficulty_level`),
  KEY `idx_rubi_ftq_tags` (`tags`),
  KEY `idx_rubi_ftq_lang` (`lang`),
  KEY `idx_rubi_ftq_is_active` (`is_active`),
  
  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_ftq_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_ftq_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Short answer questions for vocabulary practice';

CREATE TABLE `rubi_true_false_question` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `question` varchar(500) NOT NULL COMMENT 'The question text',
  `answer` enum('TRUE','FALSE') NOT NULL COMMENT 'Correct answer for the question',
  `explanation` varchar(1000) DEFAULT NULL COMMENT 'Explanation for the correct answer',
  `difficulty_level` varchar(20) DEFAULT NULL COMMENT 'Difficulty level of the question',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT 'Number of times users answered incorrectly',
  `success_count` int NOT NULL DEFAULT '0' COMMENT 'Number of times users answered correctly',

  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the question content',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Order for display (lower = higher priority)',

  -- Audit Trail
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',

  -- Soft Delete
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rubi_tf_question_lang` (`question`, `lang`),

  -- Indexes
  KEY `idx_rubi_tf_difficulty` (`difficulty_level`),
  KEY `idx_rubi_tf_tags` (`tags`),
  KEY `idx_rubi_tf_lang` (`lang`),
  KEY `idx_rubi_tf_is_active` (`is_active`),

  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_tf_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_tf_updated_by`
    FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='True/False questions for vocabulary practice';

CREATE TABLE `rubi_fill_blank_question` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `question` varchar(500) NOT NULL COMMENT 'Question text with blank(s), e.g. "I ___ to school yesterday."',
  `answer` varchar(200) NOT NULL COMMENT 'Comma-separated correct answers for the blank',
  `explanation` varchar(1000) DEFAULT NULL COMMENT 'Explanation for the correct answer(s)',
  `difficulty_level` varchar(20) DEFAULT NULL COMMENT 'Difficulty level of the question',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT 'Number of times users answered incorrectly',
  `success_count` int NOT NULL DEFAULT '0' COMMENT 'Number of times users answered correctly',

  `tags` varchar(500) DEFAULT NULL COMMENT 'Comma-separated tags for categorization and search',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the question content',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Order for display (lower = higher priority)',

  -- Audit Trail
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',

  -- Soft Delete
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rubi_fb_question_lang` (`question`, `lang`),

  -- Indexes
  KEY `idx_rubi_fb_difficulty` (`difficulty_level`),
  KEY `idx_rubi_fb_tags` (`tags`),
  KEY `idx_rubi_fb_lang` (`lang`),
  KEY `idx_rubi_fb_is_active` (`is_active`),

  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_fb_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_fb_updated_by`
    FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Fill-in-the-blank questions for vocabulary practice';

CREATE TABLE `rubi_question_image` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `multiple_choice_question_id` char(36) DEFAULT NULL COMMENT 'Associated Multiple Choice Question ID',
  `free_text_question_id` char(36) DEFAULT NULL COMMENT 'Associated Free Text Question ID',
  `true_false_question_id` char(36) DEFAULT NULL COMMENT 'Associated True/False Question ID',
  `fill_blank_question_id` char(36) DEFAULT NULL COMMENT 'Associated Fill-in-the-Blank Question ID',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `width` smallint UNSIGNED DEFAULT NULL COMMENT 'Image width in pixels',
  `height` smallint UNSIGNED DEFAULT NULL COMMENT 'Image height in pixels',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Content language',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Display order (lower = higher priority)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rubi_question_answer_image_filename` (`filename`),
  
  INDEX `idx_active_lang_order` (`is_active`, `lang`, `display_order`),
  INDEX `idx_filename` (`filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`),

  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_qi_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_qi_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Images associated with questions and answers';

CREATE TABLE `rubi_image` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Rubi Image Management';

CREATE TABLE `rubi_video` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Rubi Video Management';

CREATE TABLE `rubi_audio` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `name` varchar(255) NOT NULL COMMENT 'Audio display name',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `source_name` varchar(255) DEFAULT NULL COMMENT 'Audio source name',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `size_bytes` bigint UNSIGNED DEFAULT NULL COMMENT 'File size in bytes',
  `cover_image_filename` varchar(500) DEFAULT NULL COMMENT 'Cover image filename (stored in uploads)',
  `description` varchar(500) DEFAULT NULL COMMENT 'Audio description',
  `subtitle` text DEFAULT NULL COMMENT 'Subtitle or transcript',
  `artist` varchar(255) DEFAULT NULL COMMENT 'Artist or creator name',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Rubi Audio Management';

CREATE TABLE `rubi_article` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Rubi Article Management';


CREATE TABLE `rubi_article_image` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `article_ru_id` char(36) NOT NULL COMMENT 'Associated article ID',
  `article_ru_title` varchar(500) DEFAULT NULL COMMENT 'Associated article title (for quick reference)',
  `filename` varchar(255) NOT NULL COMMENT 'Stored filename',
  `original_url` varchar(500) DEFAULT NULL COMMENT 'Original source URL',
  `width` smallint UNSIGNED DEFAULT NULL COMMENT 'Image width in pixels',
  `height` smallint UNSIGNED DEFAULT NULL COMMENT 'Image height in pixels',
  `lang` enum('EN','ZH') NOT NULL DEFAULT 'EN' COMMENT 'Content language',
  `display_order` int NOT NULL DEFAULT '0' COMMENT 'Display order (lower = higher priority)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  `created_by` char(36) DEFAULT NULL COMMENT 'Created by user ID',
  `updated_by` char(36) DEFAULT NULL COMMENT 'Last updated by user ID',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Active status flag',

  PRIMARY KEY (`id`),
  INDEX `idx_article_ru_id` (`article_ru_id`),
  INDEX `idx_active_lang_order` (`is_active`, `lang`, `display_order`),
  INDEX `idx_filename` (`filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Rubi Article Images';

