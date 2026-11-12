-- ===================================================================
-- CampusConnect - Initial Seed Data (MySQL 8)
-- CGC University Mohali - College Event Management & Registration
-- ===================================================================

USE campus_connect;

-- 1. Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'College Administrator'),
(2, 'ROLE_INTERNAL_STUDENT', 'Internal College Student (CGC Mohali)'),
(3, 'ROLE_EXTERNAL_STUDENT', 'External College Student')
ON DUPLICATE KEY UPDATE name=name;

-- 2. Colleges
INSERT INTO colleges (id, name, code, city, state, is_internal) VALUES
(1, 'CGC University Mohali', 'CGC', 'Mohali', 'Punjab', TRUE),
(2, 'Thapar Institute of Engineering and Technology', 'TIET', 'Patiala', 'Punjab', FALSE),
(3, 'Chitkara University', 'CU-PB', 'Rajpura', 'Punjab', FALSE),
(4, 'Punjab Engineering College', 'PEC', 'Chandigarh', 'Chandigarh', FALSE)
ON DUPLICATE KEY UPDATE name=name;

-- 3. Default Users (BCrypt encrypted password: Admin@123 -> $2a$10$7ZkQ6k0v... or Student@123)
-- Password for admin: Admin@123 ($2a$10$WwL19e48GqN6VdKk3jH1m.J4yI.g7x4b0Q1vK8wF2e1m7a2b3c4d5)
-- Password for students: Student@123 ($2a$10$EwL29e58GqN6VdKk3jH1m.J4yI.g7x4b0Q1vK8wF2e1m7a2b3c4d5)

INSERT INTO users (id, full_name, email, password, phone, roll_number, department, semester, college_id, external_college_name, status, approval_notes, approved_at, created_at, updated_at) VALUES
(1, 'Dr. Harpreet Singh', 'admin@cgc.edu.in', '$2a$10$4B9h3b6Lw1V9/2rCj4H9te9n3Fq6yZ8K7gL4uP1wT0vR2e5m8a9b0', '9876543210', 'EMP001', 'Student Welfare Department', NULL, 1, NULL, 'ACTIVE', 'System Admin Account', NOW(), NOW(), NOW()),
(2, 'Rahul Sharma', 'rahul.cse@cgc.edu.in', '$2a$10$4B9h3b6Lw1V9/2rCj4H9te9n3Fq6yZ8K7gL4uP1wT0vR2e5m8a9b0', '9812345678', '2023CSE1042', 'Computer Science & Engineering', '6th Semester', 1, NULL, 'ACTIVE', 'Verified CGC student', NOW(), NOW(), NOW()),
(3, 'Priya Verma', 'priya.sharma@thapar.edu', '$2a$10$4B9h3b6Lw1V9/2rCj4H9te9n3Fq6yZ8K7gL4uP1wT0vR2e5m8a9b0', '9876501234', 'TIET-2023-CS55', 'Computer Engineering', NULL, 2, 'Thapar Institute of Engineering and Technology', 'ACTIVE', 'Verified student ID card', NOW(), NOW(), NOW()),
(4, 'Aman Verma', 'aman.verma@chitkara.edu.in', '$2a$10$4B9h3b6Lw1V9/2rCj4H9te9n3Fq6yZ8K7gL4uP1wT0vR2e5m8a9b0', '9823456789', 'CU2023-IT19', 'Information Technology', NULL, 3, 'Chitkara University', 'PENDING_APPROVAL', NULL, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE email=email;

-- 4. User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3)
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 5. Categories
INSERT INTO event_categories (id, name, code, description, icon, color_hex) VALUES
(1, 'Technical & Hackathons', 'TECH', 'Coding, hackathons, robotics and technical symposiums', 'bi-code-slash', '#6366f1'),
(2, 'Cultural & Arts', 'CULTURE', 'Music, dance, theatre, fashion shows and fine arts', 'bi-palette', '#ec4899'),
(3, 'Sports & Athletics', 'SPORTS', 'Cricket, football, badminton and athletics', 'bi-trophy', '#10b981'),
(4, 'Workshops & Seminars', 'WORKSHOP', 'Skill masterclasses, guest lectures and tech tracks', 'bi-mortarboard', '#3b82f6'),
(5, 'Management & Quizzes', 'MGMT', 'Business case study, quizzes and startup pitch deck', 'bi-lightbulb', '#f59e0b')
ON DUPLICATE KEY UPDATE code=code;

-- 6. Venues
INSERT INTO venues (id, name, building, room_number, capacity, facilities, active) VALUES
(1, 'CGC Main Auditorium', 'Admin Block A', 'Auditorium Hall', 800, 'Dual 4K Projectors, Stage Lighting, AC, Sound System', TRUE),
(2, 'Seminar Hall Block 3', 'Engineering Block 3', 'Room 301', 200, 'Podium, Mic setup, Smartboard, AC', TRUE),
(3, 'Tech Lab Alpha', 'CSE Block 2', 'Lab 204', 120, '120 High-spec workstations, Gigabit LAN, UPS', TRUE),
(4, 'University Sports Arena', 'Sports Pavilion', 'Ground 1', 1200, 'Floodlights, Pavilion, First Aid Booth', TRUE)
ON DUPLICATE KEY UPDATE name=name;

