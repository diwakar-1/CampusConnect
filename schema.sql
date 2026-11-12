-- ===================================================================
-- CampusConnect - Database Schema Reference (MySQL 8)
-- CGC University Mohali - College Event Management & Registration
-- ===================================================================

CREATE DATABASE IF NOT EXISTS campus_connect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_connect;

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(150)
) ENGINE=InnoDB;

-- 2. Colleges
CREATE TABLE IF NOT EXISTS colleges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50),
    city VARCHAR(100),
    state VARCHAR(100),
    is_internal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

-- 3. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(25),
    roll_number VARCHAR(60),
    department VARCHAR(100),
    semester VARCHAR(30),
    college_id BIGINT,
    external_college_name VARCHAR(180),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    approval_notes VARCHAR(500),
    approved_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_users_college FOREIGN KEY (college_id) REFERENCES colleges (id) ON DELETE SET NULL,
    INDEX idx_user_email (email),
    INDEX idx_user_status (status)
) ENGINE=InnoDB;

-- 4. User Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Event Categories
CREATE TABLE IF NOT EXISTS event_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon VARCHAR(50),
    color_hex VARCHAR(20)
) ENGINE=InnoDB;

-- 6. Venues
CREATE TABLE IF NOT EXISTS venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    building VARCHAR(100),
    room_number VARCHAR(50),
    capacity INT NOT NULL DEFAULT 100,
    facilities VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 7. Events
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    banner_image_url VARCHAR(500),
    category_id BIGINT NOT NULL,
    venue_id BIGINT,
    custom_venue_details VARCHAR(200),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    registration_start_date DATETIME,
    registration_deadline DATETIME NOT NULL,
    max_capacity INT NOT NULL DEFAULT 100,
    current_registrations INT NOT NULL DEFAULT 0,
    registration_fee DOUBLE NOT NULL DEFAULT 0.0,
    coordinator_name VARCHAR(120),
    coordinator_email VARCHAR(150),
    coordinator_phone VARCHAR(30),
    eligibility_requirements VARCHAR(500),
    visibility VARCHAR(30) NOT NULL DEFAULT 'ALL',
    status VARCHAR(30) NOT NULL DEFAULT 'UPCOMING',
    published BOOLEAN NOT NULL DEFAULT TRUE,
    allow_team_registration BOOLEAN NOT NULL DEFAULT FALSE,
    min_team_size INT DEFAULT 1,
    max_team_size INT DEFAULT 4,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES event_categories (id),
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE SET NULL,
    INDEX idx_event_status (status),
    INDEX idx_event_visibility (visibility),
    INDEX idx_event_published (published)
) ENGINE=InnoDB;

-- 8. Event Schedules / Sessions
CREATE TABLE IF NOT EXISTS event_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    session_title VARCHAR(150) NOT NULL,
    session_description VARCHAR(500),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    venue_id BIGINT,
    coordinator_name VARCHAR(100),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_schedules_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedules_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE SET NULL,
    INDEX idx_schedule_event (event_id),
    INDEX idx_schedule_venue (venue_id)
) ENGINE=InnoDB;

-- 9. Teams
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(120) NOT NULL,
    event_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_teams_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_teams_leader FOREIGN KEY (leader_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Team Members
CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    roll_number VARCHAR(60),
    college_name VARCHAR(180),
    phone VARCHAR(25),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Event Registrations
CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(60) NOT NULL UNIQUE,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    registration_type VARCHAR(30) NOT NULL DEFAULT 'INDIVIDUAL',
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    registered_at DATETIME NOT NULL,
    qr_code_data TEXT,
    team_id BIGINT,
    CONSTRAINT uk_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE SET NULL,
    INDEX idx_reg_number (registration_number),
    INDEX idx_reg_status (status)
) ENGINE=InnoDB;

-- 12. Attendance Records
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id BIGINT NOT NULL UNIQUE,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',
    marked_at DATETIME NOT NULL,
    marked_by_id BIGINT,
    verification_method VARCHAR(50) DEFAULT 'QR_SCAN',
    notes VARCHAR(255),
    CONSTRAINT fk_att_reg FOREIGN KEY (registration_id) REFERENCES event_registrations (id) ON DELETE CASCADE,
    CONSTRAINT fk_att_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_att_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_att_marked_by FOREIGN KEY (marked_by_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_att_event (event_id),
    INDEX idx_att_user (user_id)
) ENGINE=InnoDB;

-- 13. Certificates
CREATE TABLE IF NOT EXISTS certificates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certificate_number VARCHAR(60) NOT NULL UNIQUE,
    registration_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    certificate_type VARCHAR(40) NOT NULL DEFAULT 'PARTICIPATION',
    student_name VARCHAR(120) NOT NULL,
    college_name VARCHAR(180) NOT NULL,
    issue_date DATE NOT NULL,
    authorized_signatory VARCHAR(120),
    verification_token VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_cert_reg FOREIGN KEY (registration_id) REFERENCES event_registrations (id) ON DELETE CASCADE,
    CONSTRAINT fk_cert_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_cert_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    INDEX idx_cert_event (event_id),
    INDEX idx_cert_user (user_id),
    INDEX idx_cert_token (verification_token)
) ENGINE=InnoDB;

-- 14. Feedback
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    overall_rating INT NOT NULL,
    organization_rating INT NOT NULL,
    venue_rating INT NOT NULL,
    coordination_rating INT NOT NULL,
    comments TEXT,
    submitted_at DATETIME NOT NULL,
    CONSTRAINT uk_feedback_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_feedback_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_feedback_event (event_id)
) ENGINE=InnoDB;

-- 15. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    link_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50) DEFAULT 'INFO',
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (is_read)
) ENGINE=InnoDB;

