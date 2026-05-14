-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(32),
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- User roles table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#6366F1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_category_per_user (user_id, name),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Addresses table
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    address VARCHAR(255) NOT NULL,
    blockchain VARCHAR(50) NOT NULL,
    label VARCHAR(100),
    notes TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(100),
    last_balance_check TIMESTAMP,
    total_balance DECIMAL(30,8),
    total_balance_usd DECIMAL(30,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_address_per_user (user_id, address, blockchain),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_blockchain (blockchain),
    INDEX idx_favorite (is_favorite)
);

-- Address categories junction table
CREATE TABLE IF NOT EXISTS address_categories (
    address_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (address_id, category_id),
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_address_id (address_id),
    INDEX idx_category_id (category_id)
);

-- Address balances table
CREATE TABLE IF NOT EXISTS address_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    address_id BIGINT NOT NULL,
    token_symbol VARCHAR(20) NOT NULL,
    token_name VARCHAR(50),
    token_address VARCHAR(255),
    token_decimals INT,
    balance DECIMAL(30,8),
    balance_usd DECIMAL(30,2),
    balance_btc DECIMAL(30,8),
    balance_eth DECIMAL(30,8),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_balance_per_address (address_id, token_symbol, token_address),
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    INDEX idx_address_id (address_id),
    INDEX idx_token_symbol (token_symbol)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    address_id BIGINT NOT NULL,
    tx_hash VARCHAR(255) NOT NULL,
    blockchain VARCHAR(50) NOT NULL,
    from_address VARCHAR(255),
    to_address VARCHAR(255),
    value DECIMAL(30,8),
    value_usd DECIMAL(30,2),
    token_symbol VARCHAR(20),
    token_address VARCHAR(255),
    timestamp BIGINT,
    status VARCHAR(20),
    gas_used DECIMAL(20),
    gas_price DECIMAL(20),
    block_number BIGINT,
    confirmations INT,
    note VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE,
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_address_id (address_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_blockchain (blockchain)
);

-- Activity logs table
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    resource VARCHAR(100),
    resource_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, enabled) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', TRUE);

INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_USER');

-- Insert demo user (password: demo123)
INSERT INTO users (username, email, password_hash, enabled) VALUES 
('demo_user', 'demo@example.com', '$2a$10$r0pK5XjZBx5wB1X4X7J4QeVvWJ8KvZJc3Y6J2M5cFjFqYb9hJmYsS', TRUE);

INSERT INTO user_roles (user_id, role) VALUES (2, 'ROLE_USER');

-- Insert sample categories for demo user
INSERT INTO categories (user_id, name, description, color) VALUES 
(2, 'Personal', 'Personal wallet addresses', '#10B981'),
(2, 'Exchange', 'Cryptocurrency exchange addresses', '#F59E0B'),
(2, 'Smart Contract', 'Smart contract addresses', '#6366F1'),
(2, 'Business', 'Business-related addresses', '#EF4444'),
(2, 'Savings', 'Long-term savings addresses', '#8B5CF6');

-- Insert sample addresses for demo user
INSERT INTO addresses (user_id, address, blockchain, label, notes, is_favorite, total_balance_usd) VALUES 
(2, '0x742d35Cc6634C0532925a3b844Bc454e4438f44e', 'ethereum', 'Main Wallet', 'Personal Ethereum wallet', TRUE, 2500.50),
(2, '1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa', 'bitcoin', 'Bitcoin Savings', 'Old Bitcoin address', FALSE, 50000.00),
(2, '0x1234567890123456789012345678901234567890', 'polygon', 'DeFi Wallet', 'For DeFi activities', TRUE, 1500.25);

-- Link addresses to categories
INSERT INTO address_categories (address_id, category_id) VALUES 
(1, 1),  -- Main Wallet -> Personal
(2, 1),  -- Bitcoin Savings -> Personal
(2, 5),  -- Bitcoin Savings -> Savings
(3, 1),  -- DeFi Wallet -> Personal
(3, 3);  -- DeFi Wallet -> Smart Contract

-- Create indexes for better performance
CREATE INDEX idx_addresses_user_blockchain ON addresses(user_id, blockchain);
CREATE INDEX idx_transactions_address_timestamp ON transactions(address_id, timestamp DESC);
CREATE INDEX idx_balances_address_token ON address_balances(address_id, token_symbol);