-- 智能问诊平台完整数据库脚本
-- 包含数据库创建、表结构和模拟数据

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hospital CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital;

-- ============================================
-- 表结构创建
-- ============================================

-- 用户表（包含患者、医生、管理员）
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    phone VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(100),
    role ENUM('patient', 'doctor', 'admin') DEFAULT 'patient',
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 患者信息表
CREATE TABLE patients (
    patient_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    id_card VARCHAR(18) NOT NULL UNIQUE,
    gender ENUM('male', 'female'),
    age INT,
    address VARCHAR(255),
    balance DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 医生信息表
CREATE TABLE doctors (
    doctor_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    dept_id INT NOT NULL,
    title VARCHAR(50),
    specialty VARCHAR(100),
    bio TEXT,
    status ENUM('available', 'unavailable') DEFAULT 'available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 管理员信息表
CREATE TABLE admins (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    department VARCHAR(100),
    position VARCHAR(100),
    permissions TEXT,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 科室表
CREATE TABLE departments (
    dept_id INT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 排班表（按星期几统一排班）
CREATE TABLE schedules (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    doctor_id INT NOT NULL,
    day_of_week ENUM('1', '2', '3', '4', '5', '6', '7') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available_slots INT NOT NULL,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    UNIQUE KEY unique_schedule (doctor_id, day_of_week, start_time)
);

-- 预约挂号表
CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    schedule_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('pending', 'confirmed', 'cancelled', 'completed') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id)
);

-- 就诊记录表
CREATE TABLE consultations (
    consultation_id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_id INT NOT NULL,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    symptoms TEXT,
    diagnosis TEXT,
    treatment TEXT,
    prescription TEXT,
    consultation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- 住院登记表
CREATE TABLE hospitalizations (
    hospitalization_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    dept_id INT NOT NULL,
    admission_date TIMESTAMP NOT NULL,
    discharge_date TIMESTAMP,
    reason TEXT,
    status ENUM('admitted', 'discharged') DEFAULT 'admitted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
);

-- 系统公告表
CREATE TABLE notices (
    notice_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(50),
    publish_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 充值记录表
CREATE TABLE recharge_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    recharge_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('online', 'offline'),
    status ENUM('success', 'failed') DEFAULT 'success',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- 缴费记录表
CREATE TABLE payment_records (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    consultation_id INT,
    hospitalization_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('online', 'offline'),
    status ENUM('paid', 'unpaid') DEFAULT 'unpaid',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (consultation_id) REFERENCES consultations(consultation_id),
    FOREIGN KEY (hospitalization_id) REFERENCES hospitalizations(hospitalization_id)
);

-- ============================================
-- 模拟数据插入
-- ============================================

-- 插入管理员数据
INSERT INTO users (username, password, real_name, phone, email, role, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800138000', 'admin@hospital.com', 'admin', 'active');

INSERT INTO admins (user_id, department, position, permissions) VALUES
(1, '信息科', '系统管理员', 'all');

-- 插入科室数据
INSERT INTO departments (dept_name, description, location) VALUES
('内科', '内科诊疗科室', '门诊楼1楼'),
('外科', '外科诊疗科室', '门诊楼2楼'),
('儿科', '儿科诊疗科室', '门诊楼3楼'),
('妇产科', '妇产科诊疗科室', '门诊楼4楼'),
('眼科', '眼科诊疗科室', '门诊楼5楼'),
('耳鼻喉科', '耳鼻喉科诊疗科室', '门诊楼5楼'),
('口腔科', '口腔科诊疗科室', '门诊楼6楼'),
('皮肤科', '皮肤科诊疗科室', '门诊楼6楼');

-- 插入医生数据
INSERT INTO users (username, password, real_name, phone, email, role, status) VALUES
('doctor1', 'e10adc3949ba59abbe56e057f20f883e', '张医生', '13800138001', 'zhang@hospital.com', 'doctor', 'active'),
('doctor2', 'e10adc3949ba59abbe56e057f20f883e', '李医生', '13800138002', 'li@hospital.com', 'doctor', 'active'),
('doctor3', 'e10adc3949ba59abbe56e057f20f883e', '王医生', '13800138003', 'wang@hospital.com', 'doctor', 'active'),
('doctor4', 'e10adc3949ba59abbe56e057f20f883e', '刘医生', '13800138004', 'liu@hospital.com', 'doctor', 'active'),
('doctor5', 'e10adc3949ba59abbe56e057f20f883e', '陈医生', '13800138005', 'chen@hospital.com', 'doctor', 'active'),
('doctor6', 'e10adc3949ba59abbe56e057f20f883e', '赵医生', '13800138006', 'zhao@hospital.com', 'doctor', 'active');

INSERT INTO doctors (user_id, dept_id, title, specialty, bio, status) VALUES
(2, 1, '主任医师', '心血管疾病', '从事心血管疾病诊疗20年，经验丰富', 'available'),
(3, 1, '副主任医师', '消化系统疾病', '擅长消化系统疾病的诊断和治疗', 'available'),
(4, 2, '主任医师', '普通外科', '普通外科专家，擅长腹腔镜手术', 'available'),
(5, 3, '副主任医师', '儿科常见病', '儿科专家，擅长儿童常见病治疗', 'available'),
(6, 4, '主任医师', '妇科疾病', '妇科专家，擅长妇科疾病诊疗', 'available'),
(7, 5, '副主任医师', '眼科疾病', '眼科专家，擅长近视、白内障治疗', 'available');

-- 插入患者数据
INSERT INTO users (username, password, real_name, phone, email, role, status) VALUES
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13900139001', 'zhangsan@email.com', 'patient', 'active'),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13900139002', 'lisi@email.com', 'patient', 'active'),
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13900139003', 'wangwu@email.com', 'patient', 'active'),
('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '13900139004', 'zhaoliu@email.com', 'patient', 'active'),
('qianqi', 'e10adc3949ba59abbe56e057f20f883e', '钱七', '13900139005', 'qianqi@email.com', 'patient', 'active');

INSERT INTO patients (user_id, id_card, gender, age, address, balance) VALUES
(8, '110101199001011234', 'male', 35, '北京市朝阳区', 1000.00),
(9, '110101199202025678', 'female', 32, '北京市海淀区', 500.00),
(10, '110101199303039012', 'male', 31, '北京市西城区', 800.00),
(11, '110101199404045678', 'female', 30, '北京市东城区', 1200.00),
(12, '110101199505057890', 'male', 29, '北京市丰台区', 600.00);

-- 插入排班数据
INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time, available_slots, status) VALUES
(1, '1', '08:00:00', '12:00:00', 20, 'active'),
(1, '1', '14:00:00', '18:00:00', 20, 'active'),
(1, '3', '08:00:00', '12:00:00', 20, 'active'),
(1, '3', '14:00:00', '18:00:00', 20, 'active'),
(1, '5', '08:00:00', '12:00:00', 20, 'active'),
(2, '2', '08:00:00', '12:00:00', 20, 'active'),
(2, '2', '14:00:00', '18:00:00', 20, 'active'),
(2, '4', '08:00:00', '12:00:00', 20, 'active'),
(2, '4', '14:00:00', '18:00:00', 20, 'active'),
(3, '1', '08:00:00', '12:00:00', 15, 'active'),
(3, '1', '14:00:00', '18:00:00', 15, 'active'),
(3, '3', '08:00:00', '12:00:00', 15, 'active'),
(4, '2', '08:00:00', '12:00:00', 25, 'active'),
(4, '2', '14:00:00', '18:00:00', 25, 'active'),
(4, '4', '08:00:00', '12:00:00', 25, 'active'),
(5, '1', '08:00:00', '12:00:00', 20, 'active'),
(5, '3', '08:00:00', '12:00:00', 20, 'active'),
(5, '5', '08:00:00', '12:00:00', 20, 'active'),
(6, '2', '08:00:00', '12:00:00', 15, 'active'),
(6, '4', '08:00:00', '12:00:00', 15, 'active'),
(6, '6', '08:00:00', '12:00:00', 15, 'active');

-- 插入预约挂号数据
INSERT INTO appointments (patient_id, doctor_id, schedule_id, appointment_date, appointment_time, status) VALUES
(1, 1, 1, '2026-03-31', '08:30:00', 'confirmed'),
(1, 2, 6, '2026-04-01', '09:00:00', 'pending'),
(2, 3, 10, '2026-03-31', '10:00:00', 'confirmed'),
(2, 4, 13, '2026-04-01', '14:30:00', 'pending'),
(3, 5, 16, '2026-03-31', '11:00:00', 'confirmed'),
(4, 6, 19, '2026-04-01', '08:30:00', 'pending'),
(5, 1, 2, '2026-03-31', '15:00:00', 'confirmed');

-- 插入就诊记录数据
INSERT INTO consultations (appointment_id, doctor_id, patient_id, symptoms, diagnosis, treatment, prescription, consultation_date) VALUES
(1, 1, 1, '胸闷、气短、心悸', '冠心病', '药物治疗、定期复查', '阿司匹林肠溶片、硝酸甘油片', '2026-03-31 09:00:00'),
(3, 3, 2, '腹痛、恶心、呕吐', '急性胃炎', '禁食、补液、药物治疗', '奥美拉唑肠溶片、蒙脱石散', '2026-03-31 10:30:00'),
(5, 5, 3, '发热、咳嗽、咽痛', '上呼吸道感染', '休息、多饮水、药物治疗', '布洛芬缓释胶囊、阿莫西林胶囊', '2026-03-31 11:30:00'),
(7, 1, 5, '头晕、头痛', '高血压', '降压治疗、生活方式调整', '氨氯地平片、缬沙坦胶囊', '2026-03-31 15:30:00');

-- 插入住院登记数据
INSERT INTO hospitalizations (patient_id, doctor_id, dept_id, admission_date, reason, status) VALUES
(1, 1, 1, '2026-03-25 10:00:00', '冠心病，需要进一步检查和治疗', 'admitted'),
(2, 3, 1, '2026-03-26 14:00:00', '急性胃炎，需要住院观察', 'discharged'),
(3, 5, 3, '2026-03-27 09:00:00', '上呼吸道感染，需要住院治疗', 'admitted');

-- 插入系统公告数据
INSERT INTO notices (title, content, author, publish_date, status) VALUES
('医院门诊时间调整通知', '从2026年4月1日起，医院门诊时间调整为：上午8:00-12:00，下午14:00-18:00。请各位患者合理安排就诊时间。', '系统管理员', '2026-03-25 10:00:00', 'active'),
('新增科室通知', '医院新增皮肤科和口腔科，欢迎有相关需求的患者前来就诊。', '系统管理员', '2026-03-20 14:00:00', 'active'),
('节假日就诊安排', '清明节期间（4月4日-4月6日），医院门诊正常开放，急诊24小时开放。', '系统管理员', '2026-03-28 09:00:00', 'active');

-- 插入充值记录数据
INSERT INTO recharge_records (patient_id, amount, recharge_date, payment_method, status) VALUES
(1, 500.00, '2026-03-20 10:00:00', 'online', 'success'),
(1, 500.00, '2026-03-25 14:00:00', 'online', 'success'),
(2, 500.00, '2026-03-21 09:00:00', 'offline', 'success'),
(3, 800.00, '2026-03-22 11:00:00', 'online', 'success'),
(4, 1200.00, '2026-03-23 15:00:00', 'online', 'success'),
(5, 600.00, '2026-03-24 16:00:00', 'offline', 'success');

-- 插入缴费记录数据
INSERT INTO payment_records (patient_id, consultation_id, amount, payment_date, payment_method, status) VALUES
(1, 1, 200.00, '2026-03-31 10:00:00', 'online', 'paid'),
(2, 2, 150.00, '2026-03-31 11:00:00', 'online', 'paid'),
(3, 3, 100.00, '2026-03-31 12:00:00', 'online', 'paid'),
(5, 4, 180.00, '2026-03-31 16:00:00', 'online', 'paid');

INSERT INTO payment_records (patient_id, hospitalization_id, amount, payment_date, payment_method, status) VALUES
(1, 1, 3000.00, '2026-03-25 10:00:00', 'online', 'paid'),
(2, 2, 2000.00, '2026-03-26 14:00:00', 'online', 'paid'),
(3, 3, 2500.00, '2026-03-27 09:00:00', 'online', 'unpaid');
