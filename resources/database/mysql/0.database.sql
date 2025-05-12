-- Drop existing databases if they exist
DROP DATABASE IF EXISTS gjpb_en;
DROP DATABASE IF EXISTS gjpb_zh;

-- Create English content database
CREATE DATABASE gjpb_en 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create Chinese content database
CREATE DATABASE gjpb_zh 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;