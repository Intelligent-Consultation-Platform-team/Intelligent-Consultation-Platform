USE hospital;
-- 正确的BCrypt哈希值（密码123456，使用10轮盐）
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq';
SELECT 'Passwords updated successfully' AS result;