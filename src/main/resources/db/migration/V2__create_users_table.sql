-- V2__create_users_table.sql
-- Create users and authorities tables for Spring Security JdbcUserDetailsManager

-- Create users table (Spring Security standard schema)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email VARCHAR(255) UNIQUE,
    full_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create index on username for fast lookups
CREATE INDEX idx_users_username ON users(username);

-- Create authorities table (Spring Security standard schema)
CREATE TABLE authorities (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_authorities_username FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE (username, authority)
);

-- Create index on username for efficient role lookups
CREATE INDEX idx_authorities_username ON authorities(username);

-- Insert default users for testing
-- Admin user: admin / admin123
INSERT INTO users (username, password, enabled, email, full_name) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/PFm', TRUE, 'admin@bankflow.com', 'System Administrator');

-- Customer user: customer1 / customer123
INSERT INTO users (username, password, enabled, email, full_name) VALUES
('customer1', '$2a$10$VcJFgQH2MjECmEYGNFRXC.4vf7vDxvLT3V3Wlwvvg1/m/ybG0Yl2a', TRUE, 'customer1@bankflow.com', 'John Doe');

-- Insert authorities for admin user
INSERT INTO authorities (username, authority) VALUES
('admin', 'ROLE_ADMIN');

-- Insert authorities for customer user
INSERT INTO authorities (username, authority) VALUES
('customer1', 'ROLE_CUSTOMER');

