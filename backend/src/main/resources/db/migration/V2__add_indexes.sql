-- Additional indexes for better query performance

-- Addresses table indexes
CREATE INDEX idx_addresses_user_favorite ON addresses(user_id, is_favorite);
CREATE INDEX idx_addresses_user_verified ON addresses(user_id, is_verified);
CREATE INDEX idx_addresses_created_at ON addresses(created_at DESC);
CREATE INDEX idx_addresses_updated_at ON addresses(updated_at DESC);

-- Categories table indexes
CREATE INDEX idx_categories_user_created ON categories(user_id, created_at DESC);
CREATE INDEX idx_categories_name ON categories(name);

-- Transactions table indexes
CREATE INDEX idx_transactions_hash_blockchain ON transactions(tx_hash, blockchain);
CREATE INDEX idx_transactions_address_status ON transactions(address_id, status);
CREATE INDEX idx_transactions_block_number ON transactions(block_number);

-- Address balances table indexes
CREATE INDEX idx_balances_token ON address_balances(token_symbol, token_address);
CREATE INDEX idx_balances_last_updated ON address_balances(last_updated);

-- Activity logs table indexes
CREATE INDEX idx_activity_logs_user_created ON activity_logs(user_id, created_at DESC);
CREATE INDEX idx_activity_logs_resource ON activity_logs(resource, resource_id);

-- Composite indexes for common queries
CREATE INDEX idx_addresses_user_blockchain_favorite ON addresses(user_id, blockchain, is_favorite);
CREATE INDEX idx_transactions_address_timestamp_status ON transactions(address_id, timestamp DESC, status);