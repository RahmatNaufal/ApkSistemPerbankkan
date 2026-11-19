-- Skema INT BANK / Sistem Perbankan (sesuai architecture.md §8)
-- Ledger bersifat append-only: aplikasi tidak pernah UPDATE/DELETE transactions.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    CONSTRAINT uq_customers_user UNIQUE (user_id),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number CHAR(10) NOT NULL,
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(19,0) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    tenor_months INT NULL,
    maturity_date DATE NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    CONSTRAINT uq_accounts_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0),
    CONSTRAINT chk_accounts_deposit CHECK (account_type <> 'DEPOSIT' OR (tenor_months IS NOT NULL AND maturity_date IS NOT NULL))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(40) NOT NULL,
    account_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(19,0) NOT NULL,
    fee DECIMAL(19,0) NOT NULL DEFAULT 0,
    balance_after DECIMAL(19,0) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(255) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(3) NOT NULL,
    CONSTRAINT uq_transactions_reference UNIQUE (reference_number),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transactions_creator FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_transactions_amount CHECK (amount > 0),
    INDEX idx_transactions_account_created (account_id, created_at DESC)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_reference VARCHAR(40) NOT NULL,
    transaction_out_id BIGINT NOT NULL,
    transaction_in_id BIGINT NOT NULL,
    source_account_id BIGINT NOT NULL,
    destination_account_id BIGINT NOT NULL,
    amount DECIMAL(19,0) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    CONSTRAINT uq_transfers_reference UNIQUE (transfer_reference),
    CONSTRAINT uq_transfers_out UNIQUE (transaction_out_id),
    CONSTRAINT uq_transfers_in UNIQUE (transaction_in_id),
    CONSTRAINT fk_transfers_out FOREIGN KEY (transaction_out_id) REFERENCES transactions (id),
    CONSTRAINT fk_transfers_in FOREIGN KEY (transaction_in_id) REFERENCES transactions (id),
    CONSTRAINT fk_transfers_source FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_dest FOREIGN KEY (destination_account_id) REFERENCES accounts (id),
    CONSTRAINT chk_transfers_diff CHECK (source_account_id <> destination_account_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    idem_key CHAR(36) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    transaction_id BIGINT NULL,
    created_at DATETIME(3) NOT NULL,
    CONSTRAINT uq_idempotency UNIQUE (user_id, idem_key),
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_idempotency_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id VARCHAR(50) NULL,
    reason VARCHAR(255) NULL,
    metadata VARCHAR(1000) NULL,
    created_at DATETIME(3) NOT NULL,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users (id),
    INDEX idx_audit_actor_created (actor_user_id, created_at)
) ENGINE=InnoDB;
