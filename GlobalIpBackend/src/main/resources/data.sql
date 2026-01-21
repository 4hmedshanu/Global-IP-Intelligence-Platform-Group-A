-- Seed admin users for automatic startup
-- Passwords: Admin123! and Admin456! (BCrypt hashed)
INSERT INTO users (username, email, password, first_name, last_name, role, created_at)
VALUES
  ('admin1', 'admin1@ipintelligence.com', '$2a$10$QeQw8Qw8Qw8Qw8Qw8Qw8QOeQw8Qw8Qw8Qw8Qw8Qw8Qw8Qw8Qw8', 'Admin', 'One', 'ADMIN', NOW()),
  ('admin2', 'admin2@ipintelligence.com', '$2a$10$ZpQw8Qw8Qw8Qw8Qw8Qw8QOeQw8Qw8Qw8Qw8Qw8Qw8Qw8Qw8Qw8', 'Admin', 'Two', 'ADMIN', NOW());
