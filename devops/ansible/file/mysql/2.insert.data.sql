-- Insert super admin user: gjpb, password: 123456
INSERT INTO auth_users (id, nickname, username, email, mobile_country_code, mobile_number, password_hash, account_status, password_changed_at, created_by,updated_by)
VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'gan', 'gjpb', 'gjpb@gmail.com', '65', '89765432', '$2a$10$PAvGvs85PZwxlV.u4c8q.u96smuyMlpcPFAXNKTlidf3F65gOfdbi', 'active', CURRENT_TIMESTAMP, NULL, NULL);

-- Insert predefined roles with hierarchical structure
INSERT INTO auth_roles (id, code, name, description, parent_role_id, level, is_system_role, sort_order, created_by, updated_by) VALUES 
-- Level 0 (Top-level roles)
('550e8400-e29b-41d4-a716-446655440001', 'SUPER_ADMIN', 'Super Administrator', 'Root-level access with all system privileges including user management and system configuration', NULL, 0, TRUE, 1, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440002', 'ADMIN', 'System Administrator', 'Full administrative access to content, users, and most system features', NULL, 0, TRUE, 2, NULL, NULL),
-- Level 1 (Sub-administrative roles)
('550e8400-e29b-41d4-a716-446655440003', 'CONTENT_MANAGER', 'Content Manager', 'Manages all content categories, publication workflows, and content organization', '550e8400-e29b-41d4-a716-446655440002', 1, TRUE, 3, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440004', 'USER_MANAGER', 'User Manager', 'Manages user accounts, roles, and permissions (except super admin functions)', '550e8400-e29b-41d4-a716-446655440002', 1, TRUE, 4, NULL, NULL),
-- Level 1 (Content creation and editing roles)
('550e8400-e29b-41d4-a716-446655440005', 'EDITOR', 'Senior Editor', 'Creates, edits, publishes, and manages all content with advanced editorial privileges', '550e8400-e29b-41d4-a716-446655440003', 2, FALSE, 5, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440006', 'AUTHOR', 'Content Author', 'Creates and edits own content, can publish with approval workflow', '550e8400-e29b-41d4-a716-446655440005', 3, FALSE, 6, NULL, NULL),
-- Level 1 (Moderation and support roles)
('550e8400-e29b-41d4-a716-446655440007', 'MODERATOR', 'Content Moderator', 'Reviews, moderates, and manages user-generated content and comments', '550e8400-e29b-41d4-a716-446655440003', 2, FALSE, 7, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440008', 'SUPPORT_AGENT', 'Customer Support Agent', 'Handles user inquiries, provides technical support, and manages customer relations', '550e8400-e29b-41d4-a716-446655440004', 2, FALSE, 8, NULL, NULL),
-- Level 0 (Special access roles)
('550e8400-e29b-41d4-a716-446655440009', 'API_CLIENT', 'API Integration Client', 'External system integration access with programmatic API privileges', NULL, 0, FALSE, 9, NULL, NULL),
-- Level 0 (Basic user role)
('550e8400-e29b-41d4-a716-446655440010', 'USER', 'Regular User', 'Standard authenticated user with basic reading, commenting, and profile management privileges', NULL, 0, TRUE, 10, NULL, NULL);

-- Assign SUPER_ADMIN role to gjpb
INSERT INTO auth_user_roles (user_id, role_id, granted_at, created_by, updated_by) 
VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP, NULL, NULL);

INSERT INTO bm_app_settings (id,name,value,lang,is_system,is_public,created_at,created_by,updated_at,updated_by) VALUES
	 ('22e7b52f-a026-427a-a1ec-3e4786c6d10d','logo_base_url','http://localhost:8081/api/v1/public/logos','EN',0,1,'2025-10-09 00:09:45','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-29 20:18:10','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('35e0ad4d-cbe2-4d6d-a7d2-d83f55db5a45','logo_tags','网站','ZH',0,1,'2025-10-10 00:10:52','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-10 00:10:52','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('4c161dbf-8236-44b1-a39e-01a3f58c8833','article_cover_image_base_url','http://localhost:8081/api/v1/public/articles/cover-images/','EN',0,1,'2025-10-26 09:27:47','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-29 20:18:15','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('4c6e21f7-8f8b-4da0-8ad4-14c250579242','video_tags','AI,Software,Hardware','EN',0,1,'2025-10-18 10:33:39','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-18 10:33:39','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('550e8400-e29b-41d4-a716-446655440001','app_name','GJP Blog System','EN',0,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL),
	 ('550e8400-e29b-41d4-a716-446655440002','app_version','1.0.0','EN',1,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL),
	 ('550e8400-e29b-41d4-a716-446655440003','app_description','A modern blog system','EN',0,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL),
	 ('550e8400-e29b-41d4-a716-446655440004','app_company','GJP Technology','EN',0,1,'2025-08-19 22:05:17',NULL,'2025-10-05 09:39:43','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('550e8400-e29b-41d4-a716-446655441001','app_name','GJP博客系统','ZH',0,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL),
	 ('550e8400-e29b-41d4-a716-446655441002','app_version','1.0.0','ZH',1,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL);
INSERT INTO bm_app_settings (id,name,value,lang,is_system,is_public,created_at,created_by,updated_at,updated_by) VALUES
	 ('550e8400-e29b-41d4-a716-446655441003','app_description','现代化的博客系统','ZH',0,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL),
	 ('550e8400-e29b-41d4-a716-446655441004','app_company','GJP科技','ZH',0,1,'2025-08-19 22:05:17',NULL,'2025-08-19 22:05:17',NULL),
	 ('5f6abe20-1d20-4cd7-83d3-c32927b6e435','file_tags','AI,Hardware','EN',0,1,'2025-10-28 11:54:22','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-28 11:54:22','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('6ac25892-1396-4cf1-9ddd-fb56ebd1be3d','website_tags','AI,Chat,News,Travel,Software,Hardware','EN',0,1,'2025-10-05 13:05:40','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-11-01 04:39:45','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('759bff0a-e30f-4ba2-851f-b89bc80e66ac','image_tags','Car,Singer','EN',0,1,'2025-10-15 21:43:15','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-15 21:43:15','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('84a768b9-ac29-4b42-b202-fbc73ff4a07a','website_tags','AI,新闻','ZH',0,1,'2025-10-05 13:07:52','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-05 13:07:52','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('90aaa537-0edb-450f-8fcd-54a5150fb889','logo_tags','Website,Company','EN',0,1,'2025-10-10 00:10:20','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-11 09:11:12','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('95a330c9-1300-409e-bc87-287aade38eb7','video_base_url','http://localhost:8081/api/v1/public/videos','EN',0,1,'2025-10-18 03:14:08','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-29 20:18:26','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('b9acdd38-84e0-420f-93f8-e37073e04563','image_base_url','http://localhost:8081/api/v1/public/images','EN',0,1,'2025-10-14 12:47:06','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-29 20:18:26','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('ba9e1dd0-1bdd-4ea1-9e9b-9733a4778392','audio_base_url','http://localhost:8081/api/v1/public/audios','EN',0,1,'2025-10-19 22:49:50','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-29 20:18:26','f47ac10b-58cc-4372-a567-0e02b2c3d479');
INSERT INTO bm_app_settings (id,name,value,lang,is_system,is_public,created_at,created_by,updated_at,updated_by) VALUES
	 ('c5aea033-a354-431c-baaa-b07e1bb78095','article_tags','AI,Software Development','EN',0,1,'2025-10-26 13:45:13','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-26 13:45:13','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('ea63fd3b-bfbe-4b79-b02d-d6f941dc7d45','image_tags','汽车,歌手','ZH',0,1,'2025-10-15 21:43:56','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-15 21:43:56','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('eb40717f-3cc4-4185-8df4-94359142102c','file_base_url','http://localhost:8081/api/v1/public/files','EN',0,1,'2025-10-28 11:53:35','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-29 20:18:31','f47ac10b-58cc-4372-a567-0e02b2c3d479'),
	 ('f1f1aac9-7997-4f1b-a4ac-0fdf9dfed5ce','audio_tags','AI,Music,Song','EN',0,1,'2025-10-20 03:58:40','f47ac10b-58cc-4372-a567-0e02b2c3d479','2025-10-20 03:58:40','f47ac10b-58cc-4372-a567-0e02b2c3d479');
