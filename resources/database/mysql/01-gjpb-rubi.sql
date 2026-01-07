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

// create unique key to avoid duplicate word entries in the same language
ALTER TABLE `rubi_vocabulary`
ADD CONSTRAINT `uniq_rubi_vocab_word_lang` UNIQUE (`word`, `lang`);

-- Multiple Choice Questions Table
CREATE TABLE `rubi_mcq` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `question` varchar(1000) NOT NULL COMMENT 'The question text',
  `option_a` varchar(200) DEFAULT NULL COMMENT 'Option A',
  `option_b` varchar(200) DEFAULT NULL COMMENT 'Option B',
  `option_c` varchar(200) DEFAULT NULL COMMENT 'Option C',
  `option_d` varchar(200) DEFAULT NULL COMMENT 'Option D',
  `correct_answers` varchar(10) NOT NULL COMMENT 'Comma-separated correct answer options (e.g., A,C)',
  `is_multiple_correct` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether multiple answers are correct',
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

-- Short Answer Questions Table
CREATE TABLE `rubi_saq` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `question` varchar(1000) NOT NULL COMMENT 'The question text',
  `correct_answer` varchar(1000) NOT NULL COMMENT 'The correct answer',
  `explanation` varchar(2000) DEFAULT NULL COMMENT 'Explanation for the correct answer',  
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
  UNIQUE KEY `uniq_rubi_saq_question_lang` (`question`, `lang`),
  
  -- Indexes
  KEY `idx_rubi_saq_difficulty` (`difficulty_level`),
  KEY `idx_rubi_saq_tags` (`tags`),
  KEY `idx_rubi_saq_lang` (`lang`),
  KEY `idx_rubi_saq_is_active` (`is_active`),
  
  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_saq_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_saq_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Short answer questions for vocabulary practice';

CREATE TABLE `rubi_question_image` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `mcq_id` char(36) DEFAULT NULL COMMENT 'Associated MCQ ID',
  `saq_id` char(36) DEFAULT NULL COMMENT 'Associated SAQ ID',
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
  INDEX `idx_mcq_id` (`mcq_id`),
  INDEX `idx_saq_id` (`saq_id`),
  INDEX `idx_active_lang_order` (`is_active`, `lang`, `display_order`),
  INDEX `idx_filename` (`filename`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_updated_by` (`updated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Images associated with questions and answers';