USE hospital;
-- 使用Hutool BCrypt生成的正确密码哈希值（密码123456）
UPDATE users SET password = '$2a$10$uuauZtxaqPPchzTWXz/46ulNTrZOt52LugDIEwYhYiKjgbdCPpYu6';
SELECT 'Passwords updated successfully' AS result;