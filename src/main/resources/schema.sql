-- Drop tables in reverse order of dependencies to avoid foreign key constraint errors
DROP TABLE IF EXISTS role_feature_actions;
DROP TABLE IF EXISTS role_features;
DROP TABLE IF EXISTS user_permissions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS features;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE IF NOT EXISTS users
(
    id                     BIGSERIAL PRIMARY KEY,
    username               VARCHAR(50)  NOT NULL UNIQUE,
    first_name             VARCHAR(100) NOT NULL,
    last_name              VARCHAR(100) NOT NULL,
    password               VARCHAR(255) NOT NULL,
    password_changed_at    TIMESTAMP,
    password_expires_at    TIMESTAMP,
    password_history_count INTEGER,
    failed_attempts        INTEGER               DEFAULT 0,
    locked_until           TIMESTAMP,
    email                  VARCHAR(100) NOT NULL UNIQUE,
    mobile_phone           VARCHAR(20),
    two_factor_enabled     BOOLEAN               DEFAULT FALSE,
    last_login_at          TIMESTAMP,
    last_login_ip          VARCHAR(45),
    enabled                BOOLEAN               DEFAULT TRUE,
    locked                 BOOLEAN               DEFAULT FALSE,
    system_account         BOOLEAN               DEFAULT FALSE,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(50),
    updated_at             TIMESTAMP,
    updated_by             VARCHAR(50)
);

-- Create features table with hierarchical support
CREATE TABLE IF NOT EXISTS features
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    code          VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(255),
    enabled       BOOLEAN              DEFAULT TRUE,
    display_order INTEGER,
    icon          VARCHAR(50),
    parent_id     BIGINT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(50),
    updated_at    TIMESTAMP,
    updated_by    VARCHAR(50),
    FOREIGN KEY (parent_id) REFERENCES features (id) ON DELETE SET NULL
);

-- Create roles table with hierarchical support
CREATE TABLE IF NOT EXISTS roles
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(50) NOT NULL UNIQUE,
    code         VARCHAR(50) NOT NULL UNIQUE,
    description  VARCHAR(255),
    priority     INTEGER              DEFAULT 0,
    system_role  BOOLEAN              DEFAULT FALSE,
    default_role BOOLEAN              DEFAULT FALSE,
    parent_id    BIGINT,
    expiry_date  TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(50),
    updated_at   TIMESTAMP,
    updated_by   VARCHAR(50),
    FOREIGN KEY (parent_id) REFERENCES roles (id) ON DELETE SET NULL
);

-- Create permissions table
CREATE TABLE IF NOT EXISTS permissions
(
    id                BIGSERIAL PRIMARY KEY,
    feature_id        BIGINT      NOT NULL,
    action            VARCHAR(50) NOT NULL,
    description       VARCHAR(255),
    requires_approval BOOLEAN              DEFAULT FALSE,
    constraint_policy VARCHAR(1000),
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(50),
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(50),
    FOREIGN KEY (feature_id) REFERENCES features (id) ON DELETE CASCADE,
    UNIQUE (feature_id, action)
);

-- Create role_permissions table
CREATE TABLE IF NOT EXISTS role_permissions
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Create user_roles table
CREATE TABLE IF NOT EXISTS user_roles
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT      NOT NULL,
    role_id             BIGINT      NOT NULL,
    assigned_at         TIMESTAMP   NOT NULL,
    valid_from          TIMESTAMP,
    valid_to            TIMESTAMP,
    assigned_by_id      BIGINT,
    assignment_reason   VARCHAR(500),
    status              VARCHAR(20) NOT NULL,
    approved_by_id      BIGINT,
    approved_at         TIMESTAMP,
    active              BOOLEAN              DEFAULT TRUE,
    inherit_permissions BOOLEAN              DEFAULT TRUE,
    restrictions        VARCHAR(1000),
    approver_notes      VARCHAR(500),
    rejection_reason    VARCHAR(500),
    rejection_date      TIMESTAMP,
    revoked_by_id       BIGINT,
    revocation_date     TIMESTAMP,
    revocation_reason   VARCHAR(500),
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(50),
    UNIQUE (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by_id) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by_id) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (revoked_by_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Create user_permissions table
CREATE TABLE IF NOT EXISTS user_permissions
(
    user_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Indexes for users
CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_enabled ON users (enabled);
CREATE INDEX idx_user_locked ON users (locked);

-- Indexes for features
CREATE INDEX idx_feature_code ON features (code);
CREATE INDEX idx_feature_parent_id ON features (parent_id);

-- Indexes for roles
CREATE INDEX idx_role_name ON roles (name);
CREATE INDEX idx_role_code ON roles (code);
CREATE INDEX idx_role_parent_id ON roles (parent_id);

-- Indexes for permissions
CREATE INDEX idx_permission_feature_action ON permissions (feature_id, action);

-- Indexes for role_permissions
CREATE INDEX idx_role_permission ON role_permissions (role_id, permission_id);

-- Indexes for user_roles
CREATE INDEX idx_user_role_user_id ON user_roles (user_id);
CREATE INDEX idx_user_role_role_id ON user_roles (role_id);
CREATE INDEX idx_user_role_active ON user_roles (active);
CREATE INDEX idx_user_role_valid_to ON user_roles (valid_to);

-- Indexes for user_permissions
CREATE INDEX idx_user_permission ON user_permissions (user_id, permission_id);
