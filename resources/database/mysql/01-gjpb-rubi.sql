-- Use the new database
USE gjpb;

CREATE TABLE `rubi_vocabulary` (
  `id` char(36) NOT NULL COMMENT 'Primary Key (UUID)',
  `word` varchar(100) NOT NULL COMMENT 'The vocabulary word',
  `word_image_filename` varchar(100) DEFAULT NULL COMMENT 'Word image file path',
  `word_image_original_url` varchar(500) DEFAULT NULL COMMENT 'Word image original URL',
  `simple_past_tense` varchar(100) DEFAULT NULL COMMENT 'Past tense form (for verbs)',
  `past_perfect_tense` varchar(100) DEFAULT NULL COMMENT 'Past perfect tense form (for verbs)',
  `translation` varchar(500) DEFAULT NULL COMMENT 'translation of the word',
  `synonyms` varchar(200) DEFAULT NULL COMMENT 'Comma-separated synonyms',
  `plural_form` varchar(100) DEFAULT NULL COMMENT 'Plural form (for nouns)',

  `phonetic` varchar(100) DEFAULT NULL COMMENT 'Phonetic transcription',
  `phonetic_audio_filename` varchar(100) DEFAULT NULL COMMENT 'Phonetic audio file path',
  `phonetic_audio_original_url` varchar(500) DEFAULT NULL COMMENT 'Phonetic audio original URL',

  `part_of_speech` varchar(50) DEFAULT NULL COMMENT 'Part of speech (noun, verb, etc.)',
  `definition` varchar(2000) NOT NULL COMMENT 'Definition of the word',
  `example` varchar(2000) DEFAULT NULL COMMENT 'Example sentence using the word',
  `dictionary_url` varchar(500) DEFAULT NULL COMMENT 'Link to an online dictionary entry',
  
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

  -- Indexes
  KEY `idx_rubi_vocab_word` (`word`),
  KEY `idx_rubi_vocab_tags` (`tags`),
  KEY `idx_rubi_vocab_lang` (`lang`),
  KEY `idx_rubi_vocab_is_active` (`is_active`),
  
  -- Foreign Key Constraints
  CONSTRAINT `fk_rubi_vocab_created_by` FOREIGN KEY (`created_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_rubi_vocab_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `auth_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vocabulary words for language learning';
