-- Production MySQL Schema Definition
-- Database: auth_system

CREATE DATABASE IF NOT EXISTS `auth_system`;
USE `auth_system`;

-- Table structure for table `users`
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL, -- Stored as BCrypt hash (typically 60 characters, but 255 provides future-proofing)
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_users_email` (`email`) -- Index on email for fast lookups during authentication
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
