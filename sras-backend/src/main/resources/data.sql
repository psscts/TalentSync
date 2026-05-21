-- ============================================================
-- TalentSync SRAS – Seed Data
-- All user passwords: Password@123
-- BCrypt-encoded with cost=10 using Spring Security 7
-- ============================================================
-- To auto-run on startup, add to application.properties:
--   spring.sql.init.mode=always
--   spring.jpa.defer-datasource-initialization=true
-- NOTE: Designed for a fresh/empty sras_db. Running this
--       script a second time on the same database will
--       silently skip rows with conflicting PKs/unique keys
--       (INSERT IGNORE), but element-collection tables have
--       no PK so duplicates accumulate – truncate them first
--       if you need to re-seed.
-- ============================================================
 
SET FOREIGN_KEY_CHECKS = 0;
 
-- ============================================================
-- 1. USERS  (12 rows: 10 EMPLOYEE + 2 PROJECT_MANAGER)
-- Unique column: email
-- ============================================================
INSERT IGNORE INTO users
    (id, email, password, role, reset_token, reset_token_expiry, created_at, updated_at)
VALUES
(1,  'arjun.sharma@cognizant.com',   '$2a$10$tkJAoAOSpHsMYXvRebMrgOG8aqc/5GAFqD4UbKHIQFuN51MhB3s0u', 'EMPLOYEE',        NULL, NULL, '2024-01-15 09:00:00', '2024-01-15 09:00:00'),
(2,  'priya.mehta@cognizant.com',    '$2a$10$3YO4rPr/lR9yHRkIYEjat.L27aIA.axNP0qhbD0jdfK7fF8DQd4My', 'EMPLOYEE',        NULL, NULL, '2022-06-01 09:00:00', '2022-06-01 09:00:00'),
(3,  'ravi.kumar@cognizant.com',     '$2a$10$LkOXLn.NaLS/6aNTSmboE.zU7vt7xzM4oWctcVOVva4WPkuOXPIM2', 'EMPLOYEE',        NULL, NULL, '2019-03-10 09:00:00', '2019-03-10 09:00:00'),
(4,  'ananya.singh@cognizant.com',   '$2a$10$AGu6iKz/vJeg5I8751ohB.4.M3ErxuWfWAbExXwfgAPaOuEXMePWK', 'EMPLOYEE',        NULL, NULL, '2021-08-20 09:00:00', '2021-08-20 09:00:00'),
(5,  'rahul.iyer@cognizant.com',     '$2a$10$DgMCo0c28zy1FVUvo6T8decRKYJOIZ5YNfxXvf6RJ/dsESKqzxlp6', 'EMPLOYEE',        NULL, NULL, '2023-04-05 09:00:00', '2023-04-05 09:00:00'),
(6,  'deepika.patel@cognizant.com',  '$2a$10$jjg3x9ZbQPSj9lg5W3C74Ojw16Kp5ZQrxlPB1MWFaxrfo9qcOPP.2', 'EMPLOYEE',        NULL, NULL, '2022-11-12 09:00:00', '2022-11-12 09:00:00'),
(7,  'vikram.nair@cognizant.com',    '$2a$10$VEq.W21QORFUrgaA7QPqLeTx5Qdi4nQhCWHAlMpVvjeliOHBHuOE2', 'EMPLOYEE',        NULL, NULL, '2018-07-22 09:00:00', '2018-07-22 09:00:00'),
(8,  'sneha.reddy@cognizant.com',    '$2a$10$x86pKeghF/T2/U2JfNiCU.gZ53YcYpemDr9BmSh0GZReyvEdkNKw.', 'EMPLOYEE',        NULL, NULL, '2015-02-28 09:00:00', '2015-02-28 09:00:00'),
(9,  'aditya.joshi@cognizant.com',   '$2a$10$dzbql6rZX1nRl.uc6F73bemGE2a05/Rn1RCwT6D7vyVOlWchY7IWG', 'EMPLOYEE',        NULL, NULL, '2024-03-18 09:00:00', '2024-03-18 09:00:00'),
(10, 'pooja.verma@cognizant.com',    '$2a$10$sssFLKj96ipQYiC/S3QBqem6aekIDts8k7gnsK8iRc/HakpncT/RO', 'EMPLOYEE',        NULL, NULL, '2020-09-14 09:00:00', '2020-09-14 09:00:00'),
(11, 'suresh.menon@cognizant.com',   '$2a$10$KPTvbzBO/5o44Tx47giM8eg7vawPrbOi1lUGts3Y79fCe38C6musq',  'PROJECT_MANAGER', NULL, NULL, '2016-05-10 09:00:00', '2016-05-10 09:00:00'),
(12, 'kavitha.rao@cognizant.com',    '$2a$10$jNuEbyuDowK5WPELM/PyeObMQjyfHcZ6YOvvI8VY0r.w5Ow8Xe0ey', 'PROJECT_MANAGER', NULL, NULL, '2017-09-03 09:00:00', '2017-09-03 09:00:00');
 
-- ============================================================
-- 2. ROLES  (10 rows)
-- ExperienceLevel: JUNIOR | MID | SENIOR | LEAD
-- WorkMode:        REMOTE | HYBRID | ONSITE
-- ============================================================
INSERT IGNORE INTO roles
    (id, name, experience_level, years_of_experience, expected_salary, work_mode)
VALUES
(1,  'Java Backend Developer',      'JUNIOR', 1,  600000,  'REMOTE'),
(2,  'React Frontend Developer',    'MID',    3,  900000,  'HYBRID'),
(3,  'DevOps Engineer',             'SENIOR', 6,  1400000, 'ONSITE'),
(4,  'Data Scientist',              'MID',    4,  1100000, 'REMOTE'),
(5,  'Business Analyst',            'JUNIOR', 2,  700000,  'HYBRID'),
(6,  'QA Engineer',                 'MID',    3,  800000,  'HYBRID'),
(7,  'Cloud Architect',             'LEAD',   9,  2200000, 'REMOTE'),
(8,  'Full Stack Developer',        'MID',    4,  1000000, 'HYBRID'),
(9,  'Machine Learning Engineer',   'SENIOR', 7,  1600000, 'REMOTE'),
(10, 'Scrum Master',                'SENIOR', 6,  1300000, 'HYBRID');
 
-- ============================================================
-- 3. ROLE REQUIRED SKILLS  (@ElementCollection)
-- One primary skill per role (10 rows)
-- ============================================================
INSERT INTO role_required_skills (role_id, skill_name) VALUES
(1,  'Java'),
(2,  'React'),
(3,  'Docker'),
(4,  'Python'),
(5,  'SQL'),
(6,  'Selenium'),
(7,  'AWS'),
(8,  'Spring Boot'),
(9,  'Machine Learning'),
(10, 'Agile');
 
-- ============================================================
-- 4. ROLE REQUIRED CERTIFICATIONS  (@ElementCollection)
-- One certification per role (10 rows)
-- ============================================================
INSERT INTO role_required_certifications (role_id, certification_name) VALUES
(1,  'Spring Professional Certification'),
(2,  'Meta Frontend Developer Certificate'),
(3,  'Certified Kubernetes Administrator'),
(4,  'Google Professional Data Engineer'),
(5,  'CBAP'),
(6,  'ISTQB Foundation Level'),
(7,  'AWS Solutions Architect Professional'),
(8,  'Oracle Certified Professional Java SE'),
(9,  'Machine Learning Specialization'),
(10, 'Professional Scrum Master I');
 
-- ============================================================
-- 5. EMPLOYEES  (10 rows)
-- Unique columns: employee_id, user_id
-- AvailabilityStatus: AVAILABLE | PARTIALLY_AVAILABLE | UNAVAILABLE
-- ExperienceLevel:    JUNIOR | MID | SENIOR | LEAD
-- ============================================================
INSERT IGNORE INTO employees
    (id, employee_id, name, joining_date, experience_level, years_of_experience,
     preferred_location, availability_status, previous_ratings,
     expected_salary, employee_score, user_id)
VALUES
(1,  'EMP001', 'Arjun Sharma',   '2024-01-15', 'JUNIOR', 1,  'Mumbai',     'AVAILABLE',           4.2, 600000,  72.5,  1),
(2,  'EMP002', 'Priya Mehta',    '2022-06-01', 'MID',    3,  'Bengaluru',  'AVAILABLE',           4.5, 900000,  81.3,  2),
(3,  'EMP003', 'Ravi Kumar',     '2019-03-10', 'SENIOR', 6,  'Chennai',    'PARTIALLY_AVAILABLE', 4.7, 1400000, 88.2,  3),
(4,  'EMP004', 'Ananya Singh',   '2021-08-20', 'MID',    4,  'Hyderabad',  'AVAILABLE',           4.3, 1100000, 79.8,  4),
(5,  'EMP005', 'Rahul Iyer',     '2023-04-05', 'JUNIOR', 2,  'Pune',       'AVAILABLE',           3.9, 700000,  65.4,  5),
(6,  'EMP006', 'Deepika Patel',  '2022-11-12', 'MID',    3,  'Ahmedabad',  'PARTIALLY_AVAILABLE', 4.1, 800000,  74.6,  6),
(7,  'EMP007', 'Vikram Nair',    '2018-07-22', 'SENIOR', 7,  'Kochi',      'UNAVAILABLE',         4.8, 1600000, 91.0,  7),
(8,  'EMP008', 'Sneha Reddy',    '2015-02-28', 'LEAD',   10, 'Hyderabad',  'PARTIALLY_AVAILABLE', 4.9, 2000000, 95.5,  8),
(9,  'EMP009', 'Aditya Joshi',   '2024-03-18', 'JUNIOR', 1,  'Pune',       'AVAILABLE',           3.8, 580000,  63.2,  9),
(10, 'EMP010', 'Pooja Verma',    '2020-09-14', 'MID',    5,  'Delhi',      'AVAILABLE',           4.4, 1050000, 82.7, 10);
 
-- ============================================================
-- 6. SKILLS  (10 rows – one primary skill per employee)
-- ProficiencyLevel: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT
-- ============================================================
INSERT IGNORE INTO skills (id, name, proficiency_level, employee_id) VALUES
(1,  'Java',             'ADVANCED',     1),
(2,  'React',            'EXPERT',       2),
(3,  'Docker',           'ADVANCED',     3),
(4,  'Python',           'EXPERT',       4),
(5,  'SQL',              'INTERMEDIATE', 5),
(6,  'Selenium',         'INTERMEDIATE', 6),
(7,  'AWS',              'EXPERT',       7),
(8,  'Spring Boot',      'ADVANCED',     8),
(9,  'Angular',          'BEGINNER',     9),
(10, 'Machine Learning', 'ADVANCED',     10);
 
-- ============================================================
-- 7. CERTIFICATIONS  (10 rows – one per employee)
-- ============================================================
INSERT IGNORE INTO certifications
    (id, certificate_id, name, issuing_organization, score, employee_id)
VALUES
(1,  'CERT-AWS-001',   'AWS Solutions Architect Associate',   'Amazon Web Services',      88.0, 1),
(2,  'CERT-PY-001',    'Python Certified Entry Programmer',   'Python Software Foundation', 92.5, 2),
(3,  'CERT-CKA-001',   'Certified Kubernetes Administrator',  'CNCF',                     87.0, 3),
(4,  'CERT-GCP-001',   'Google Professional Data Engineer',   'Google Cloud',             94.0, 4),
(5,  'CERT-ISTQB-001', 'ISTQB Foundation Level',              'ISTQB',                    82.5, 5),
(6,  'CERT-SEL-001',   'Selenium WebDriver Professional',     'Udemy',                    78.0, 6),
(7,  'CERT-AWSP-001',  'AWS Solutions Architect Professional','Amazon Web Services',       96.0, 7),
(8,  'CERT-SPR-001',   'Spring Professional Certification',   'VMware',                   91.5, 8),
(9,  'CERT-AZ-001',    'Azure Fundamentals AZ-900',           'Microsoft',                80.0, 9),
(10, 'CERT-ML-001',    'Machine Learning Specialization',     'Coursera',                 93.0, 10);
 
-- ============================================================
-- 8. PROJECTS  (10 rows)
-- manager_user_id references users 11 (Suresh Menon) and 12 (Kavitha Rao)
-- ============================================================
INSERT IGNORE INTO projects
    (id, project_name, domain, start_date, end_date, manager_user_id)
VALUES
(1,  'SmartHR Portal',      'HR Technology',   '2025-01-15', '2025-12-31', 11),
(2,  'FinTrack Dashboard',  'FinTech',         '2025-02-01', '2025-10-31', 11),
(3,  'CloudMigrate Pro',    'Cloud Computing', '2025-03-01', '2026-02-28', 12),
(4,  'RetailConnect App',   'Retail',          '2025-04-01', '2025-09-30', 12),
(5,  'DataVault Analytics', 'Data Analytics',  '2025-05-01', '2026-04-30', 11),
(6,  'SecureNet Platform',  'Cybersecurity',   '2025-06-01', '2026-05-31', 11),
(7,  'EduLearn LMS',        'EdTech',          '2025-01-10', '2025-11-30', 12),
(8,  'HealthPulse App',     'HealthTech',      '2025-07-01', '2026-06-30', 12),
(9,  'LogiTrack System',    'Logistics',       '2025-08-01', '2026-07-31', 11),
(10, 'AgriSmart Platform',  'AgriTech',        '2025-09-01', '2026-08-31', 12);
 
-- ============================================================
-- 9. PROJECT LOCATION PREFERENCES  (@ElementCollection)
-- One preferred city per project (10 rows)
-- ============================================================
INSERT INTO project_location_preferences (project_id, location) VALUES
(1,  'Bengaluru'),
(2,  'Mumbai'),
(3,  'Hyderabad'),
(4,  'Chennai'),
(5,  'Pune'),
(6,  'Delhi'),
(7,  'Bengaluru'),
(8,  'Hyderabad'),
(9,  'Chennai'),
(10, 'Jaipur');
 
-- ============================================================
-- 10. PROJECT REQUIREMENTS  (10 rows)
-- Each requirement links a project to a role with a location
-- ============================================================
INSERT IGNORE INTO project_requirements
    (id, location, number_of_positions, project_id, role_id)
VALUES
(1,  'Bengaluru', 2, 1,  1),   -- SmartHR Portal        → Java Backend Developer
(2,  'Mumbai',    3, 2,  2),   -- FinTrack Dashboard    → React Frontend Developer
(3,  'Hyderabad', 1, 3,  3),   -- CloudMigrate Pro      → DevOps Engineer
(4,  'Chennai',   2, 4,  6),   -- RetailConnect App     → QA Engineer
(5,  'Pune',      1, 5,  4),   -- DataVault Analytics   → Data Scientist
(6,  'Delhi',     2, 6,  7),   -- SecureNet Platform    → Cloud Architect
(7,  'Bengaluru', 3, 7,  8),   -- EduLearn LMS          → Full Stack Developer
(8,  'Hyderabad', 2, 8,  9),   -- HealthPulse App       → ML Engineer
(9,  'Chennai',   1, 9,  3),   -- LogiTrack System      → DevOps Engineer
(10, 'Jaipur',    2, 10, 5);   -- AgriSmart Platform    → Business Analyst
 
-- ============================================================
-- 11. PROJECT ASSIGNMENTS  (10 rows)
-- Employees assigned to projects by the project managers
-- ============================================================
INSERT IGNORE INTO project_assignments
    (id, project_id, employee_id, assigned_by_user_id, assigned_at)
VALUES
(1,  1,  1,  11, '2025-01-20'),   -- Arjun Sharma   → SmartHR Portal        (Suresh Menon)
(2,  2,  2,  11, '2025-02-05'),   -- Priya Mehta    → FinTrack Dashboard    (Suresh Menon)
(3,  3,  3,  12, '2025-03-10'),   -- Ravi Kumar     → CloudMigrate Pro      (Kavitha Rao)
(4,  4,  6,  12, '2025-04-08'),   -- Deepika Patel  → RetailConnect App     (Kavitha Rao)
(5,  5,  4,  11, '2025-05-12'),   -- Ananya Singh   → DataVault Analytics   (Suresh Menon)
(6,  6,  7,  11, '2025-06-15'),   -- Vikram Nair    → SecureNet Platform    (Suresh Menon)
(7,  7,  5,  12, '2025-01-20'),   -- Rahul Iyer     → EduLearn LMS          (Kavitha Rao)
(8,  8,  10, 12, '2025-07-05'),   -- Pooja Verma    → HealthPulse App       (Kavitha Rao)
(9,  9,  8,  11, '2025-08-10'),   -- Sneha Reddy    → LogiTrack System      (Suresh Menon)
(10, 10, 9,  12, '2025-09-05');   -- Aditya Joshi   → AgriSmart Platform    (Kavitha Rao)
 
SET FOREIGN_KEY_CHECKS = 1;
 
 