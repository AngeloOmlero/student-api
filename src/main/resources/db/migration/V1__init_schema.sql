-- ===============================================
-- V1__init_schema.sql
-- Initial schema for Student API
-- ===============================================

-- Drop existing tables if any (for dev use only; remove in prod)
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS courses CASCADE;

-- ===============================================
-- COURSES TABLE
-- ===============================================
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===============================================
-- STUDENTS TABLE
-- ===============================================
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    course_id BIGINT,
    CONSTRAINT fk_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE SET NULL
);

-- ===============================================
-- AUDIT LOGS TABLE
-- ===============================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    details TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_info VARCHAR(255) NOT NULL DEFAULT 'system'
);

-- ===============================================
-- OPTIONAL INDEXES
-- ===============================================
CREATE INDEX idx_students_email ON students (email);
CREATE INDEX idx_courses_name ON courses (name);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs (timestamp);
