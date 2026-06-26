USE hospital;

INSERT INTO users (username, password, real_name, phone, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'Admin', '13800138001', 'admin');

SELECT 'Test insert' AS result;