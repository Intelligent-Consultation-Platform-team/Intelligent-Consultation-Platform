USE hospital;

INSERT INTO users (username, password, real_name, phone, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'guanliyuan', '13800138001', 'admin'),
('doctor1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'zhangyisheng', '13800138002', 'doctor'),
('doctor2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'liyisheng', '13800138003', 'doctor'),
('patient1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'wangxiaoming', '13800138004', 'patient'),
('patient2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'lixiaohua', '13800138005', 'patient');

INSERT INTO departments (dept_name, description, location) VALUES
('neike', 'Internal medicine department', 'Building 1 Floor 1'),
('waike', 'Surgery department', 'Building 1 Floor 2'),
('fuke', 'Obstetrics and gynecology', 'Building 2 Floor 1'),
('erke', 'Pediatrics department', 'Building 2 Floor 2'),
('jizhenke', 'Emergency department', 'Building 3 Floor 1'),
('pifuke', 'Dermatology department', 'Building 3 Floor 2'),
('yanke', 'Ophthalmology department', 'Building 4 Floor 1'),
('erbihouke', 'ENT department', 'Building 4 Floor 2');

INSERT INTO patients (user_id, id_card, gender, age, address, balance) VALUES
(4, '110101199001011234', 'male', 34, 'Chaoyang District', 500.00),
(5, '110102198506155678', 'female', 39, 'Haidian District', 1000.00);

INSERT INTO doctors (user_id, dept_id, title, specialty, bio) VALUES
(2, 1, 'Chief Physician', 'Cardiovascular', '20 years experience'),
(3, 2, 'Associate Chief', 'Orthopedics', '15 years experience');

INSERT INTO admins (user_id, department, position, permissions) VALUES
(1, 'IT Department', 'System Admin', 'all');

INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time, available_slots) VALUES
(1, '1', '08:00:00', '12:00:00', 10),
(1, '3', '08:00:00', '12:00:00', 10),
(1, '5', '14:00:00', '18:00:00', 10),
(2, '2', '08:00:00', '12:00:00', 10),
(2, '4', '08:00:00', '12:00:00', 10),
(2, '6', '14:00:00', '18:00:00', 10);

INSERT INTO notices (title, content, author) VALUES
('Notice', 'Please book appointment in advance', 'Admin'),
('Schedule Change', 'Doctor Zhang schedule changed', 'Admin'),
('Health Tips', 'Summer health tips', 'Admin');

INSERT INTO appointments (patient_id, doctor_id, schedule_id, appointment_date, appointment_time, status, symptoms) VALUES
(1, 1, 1, '2026-06-24', '09:00:00', 'confirmed', 'chest discomfort'),
(2, 2, 4, '2026-06-25', '10:00:00', 'pending', 'knee pain');

INSERT INTO consultations (appointment_id, doctor_id, patient_id, symptoms, diagnosis, treatment, prescription, amount) VALUES
(1, 1, 1, 'chest discomfort', 'Coronary heart disease', 'Rest recommended', 'Medicine', 200.00);

INSERT INTO payment_records (patient_id, consultation_id, amount, payment_method, status) VALUES
(1, 1, 200.00, 'wechat', 'paid');

INSERT INTO recharge_records (patient_id, amount, payment_method, status) VALUES
(1, 500.00, 'wechat', 'success'),
(2, 1000.00, 'alipay', 'success');

SELECT 'Data import completed' AS result;