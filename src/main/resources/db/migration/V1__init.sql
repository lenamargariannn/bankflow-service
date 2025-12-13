-- V1__init.sql
-- Initial BankFlow schema creation with Flyway migration
-- Creates all core tables: customers, accounts, transaction_records
-- Includes indexes and constraints for production use

-- Create sequence for customers table
CREATE SEQUENCE customers_id_seq START WITH 1 INCREMENT BY 1;

-- Create customers table
CREATE TABLE customers (
    id BIGINT PRIMARY KEY DEFAULT nextval('customers_id_seq'),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create index on email for fast lookups
CREATE INDEX idx_customers_email ON customers(email);

-- Create sequence for accounts table
CREATE SEQUENCE accounts_id_seq START WITH 1 INCREMENT BY 1;

-- Create accounts table
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY DEFAULT nextval('accounts_id_seq'),
    account_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Create indexes on accounts
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Create sequence for transaction_records table
CREATE SEQUENCE transaction_records_id_seq START WITH 1 INCREMENT BY 1;

-- Create transaction_records table
CREATE TABLE transaction_records (
    id BIGINT PRIMARY KEY DEFAULT nextval('transaction_records_id_seq'),
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    description VARCHAR(500),
    performed_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_transaction_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

-- Create indexes on transaction_records
CREATE INDEX idx_transaction_records_type ON transaction_records(type);
CREATE INDEX idx_transaction_records_from_account_id ON transaction_records(from_account_id);
CREATE INDEX idx_transaction_records_to_account_id ON transaction_records(to_account_id);
CREATE INDEX idx_transaction_records_timestamp ON transaction_records(timestamp DESC);

-- Create a composite index for efficient querying of account transactions
CREATE INDEX idx_transaction_records_account_activity
    ON transaction_records(from_account_id, to_account_id, timestamp DESC);

-- Add constraint to ensure amount is positive
ALTER TABLE transaction_records
    ADD CONSTRAINT check_transaction_amount
    CHECK (amount > 0);

-- Add constraint to ensure balance is non-negative
ALTER TABLE accounts
    ADD CONSTRAINT check_account_balance
    CHECK (balance >= 0);

