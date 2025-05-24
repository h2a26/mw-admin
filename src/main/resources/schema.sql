-- Drop the type unconditionally if it exists
DROP TYPE IF EXISTS action_enum CASCADE;

-- Then create it
CREATE TYPE action_enum AS ENUM ('CREATE', 'READ', 'UPDATE', 'DELETE', 'EXECUTE');

-- Drop tables if exist (in order of dependencies)
DROP TABLE IF EXISTS role_feature_actions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS role_features CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS features CASCADE;

-- Create tables with IF NOT EXISTS

CREATE TABLE IF NOT EXISTS features
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL,
    created_by  VARCHAR(255) NULL,
    modified_by VARCHAR(255) NULL,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_feature_name ON features (name);

CREATE TABLE IF NOT EXISTS permissions
(
    id          BIGSERIAL PRIMARY KEY,
    feature_id  BIGINT      NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    action      action_enum NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    created_by  VARCHAR(255) NULL,
    modified_by VARCHAR(255) NULL,
    version     BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_permission_feature_action ON permissions (feature_id, action);

CREATE TABLE IF NOT EXISTS roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL,
    created_by  VARCHAR(255) NULL,
    modified_by VARCHAR(255) NULL,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_role_name ON roles (name);

CREATE TABLE IF NOT EXISTS role_permissions
(
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT    NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id BIGINT    NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    created_by  VARCHAR(255) NULL,
    modified_by VARCHAR(255) NULL,
    version       BIGINT    NOT NULL DEFAULT 0,
    UNIQUE (role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permission ON role_permissions (role_id, permission_id);

CREATE TABLE IF NOT EXISTS role_features
(
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT    NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    feature_id  BIGINT    NOT NULL REFERENCES features (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    created_by  VARCHAR(255) NULL,
    modified_by VARCHAR(255) NULL,
    version     BIGINT    NOT NULL DEFAULT 0,
    UNIQUE (role_id, feature_id)
);

CREATE INDEX IF NOT EXISTS idx_role_feature ON role_features (role_id, feature_id);

CREATE TABLE IF NOT EXISTS role_feature_actions
(
    role_feature_id BIGINT      NOT NULL REFERENCES role_features (id) ON DELETE CASCADE,
    action          action_enum NOT NULL,
    PRIMARY KEY (role_feature_id, action)
);

CREATE TABLE IF NOT EXISTS users
(
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    locked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL,
    created_by  VARCHAR(255) NULL,
    modified_by VARCHAR(255) NULL,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_user_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users (email);

CREATE TABLE IF NOT EXISTS user_roles
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id             BIGINT    NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    assigned_at         TIMESTAMPTZ,
    assigned_by_id      BIGINT    NULL REFERENCES users (id),
    assignment_reason   TEXT,
    active              BOOLEAN   NOT NULL DEFAULT TRUE,
    inherit_permissions BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(255) NULL,
    modified_by         VARCHAR(255) NULL,
    version             BIGINT    NOT NULL DEFAULT 0,
    UNIQUE (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_role ON user_roles (user_id, role_id);