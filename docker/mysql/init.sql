-- Library Management System Database Initialization

-- This script runs automatically when the MySQL container starts
-- It creates the necessary database structure for the Library Management System

-- The database 'library_management' is already created by docker-compose environment variables
USE library_management;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- The application will create tables automatically via JPA/Hibernate
-- This file can be extended with additional initialization data if needed

-- Example: Insert default categories
-- INSERT INTO categories (name, description, created_at, updated_at) VALUES 
-- ('Fiction', 'Fiction books', NOW(), NOW()),
-- ('Non-Fiction', 'Non-fiction books', NOW(), NOW()),
-- ('Science', 'Science and technology books', NOW(), NOW());