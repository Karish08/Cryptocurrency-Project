-- Create database
CREATE DATABASE IF NOT EXISTS crypto_address_system;
USE crypto_address_system;

-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Address categories
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#6366F1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_category_per_user (user_id, name)
);

-- Cryptocurrency addresses
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    address VARCHAR(255) NOT NULL,
    blockchain VARCHAR(50) NOT NULL,
    label VARCHAR(100),
    notes TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_address_per_user (user_id, address, blockchain)
);

-- Address-category relationships
CREATE TABLE address_categories (
    address_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (address_id, category_id),
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Address balances (cached)
CREATE TABLE address_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    address_id BIGINT NOT NULL,
    token_symbol VARCHAR(20) NOT NULL,
    token_address VARCHAR(255),
    balance DECIMAL(30, 8),
    balance_usd DECIMAL(30, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_balance_per_address (address_id, token_symbol, token_address)
);

-- Transaction history
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    address_id BIGINT NOT NULL,
    tx_hash VARCHAR(255) NOT NULL,
    blockchain VARCHAR(50) NOT NULL,
    from_address VARCHAR(255),
    to_address VARCHAR(255),
    value DECIMAL(30, 8),
    token_symbol VARCHAR(20),
    timestamp BIGINT,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    INDEX idx_tx_hash (tx_hash)
);

-- Activity logs
CREATE TABLE activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sample data
INSERT INTO users (username, email, password_hash) VALUES 
('demo_user', 'demo@example.com', '$2a$10$YourHashedPasswordHere');

INSERT INTO categories (user_id, name, description, color) VALUES 
(1, 'Personal', 'Personal wallet addresses', '#10B981'),
(1, 'Exchange', 'Cryptocurrency exchange addresses', '#F59E0B'),
(1, 'Smart Contract', 'Smart contract addresses', '#6366F1'),
(1, 'Business', 'Business-related addresses', '#EF4444');

INSERT INTO addresses (user_id, address, blockchain, label, notes) VALUES 
(1, '0x742d35Cc6634C0532925a3b844Bc454e4438f44e', 'ethereum', 'Main Wallet', 'Personal Ethereum wallet'),
(1, '1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa', 'bitcoin', 'Bitcoin Savings', 'Old Bitcoin address');

-- Add address-category relationships
INSERT INTO address_categories (address_id, category_id) VALUES 
(1, 1),  -- Main Wallet -> Personal
(2, 1);  -- Bitcoin Savings -> Personal