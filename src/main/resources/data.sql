-- FEATURES
INSERT INTO features (id, name, description, enabled, created_at, updated_at, version)
VALUES (1, 'USER_MANAGEMENT', 'Manage users and roles', true, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (2, 'REPORTING', 'Access reports and statistics', true, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0);

-- PERMISSIONS
INSERT INTO permissions (id, feature_id, action, description, created_at, updated_at, version)
VALUES (1, 1, 'CREATE', 'Create users', '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (2, 1, 'READ', 'Read user data', '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (3, 1, 'UPDATE', 'Update users', '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (4, 1, 'DELETE', 'Delete users', '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (5, 2, 'READ', 'View reports', '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0);

-- ROLES
INSERT INTO roles (id, name, description, system_role, created_at, updated_at, version)
VALUES (1, 'ADMIN', 'Administrator role', true, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (2, 'USER', 'Standard user role', false, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0);

-- ROLE_PERMISSIONS
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, version)
VALUES (1, 1, 1, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (2, 1, 2, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (3, 1, 3, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (4, 1, 4, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (5, 1, 5, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (6, 2, 2, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (7, 2, 5, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0);

-- ROLE_FEATURES
INSERT INTO role_features (id, role_id, feature_id, created_at, updated_at, version)
VALUES (1, 1, 1, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (2, 1, 2, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (3, 2, 2, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0);

-- ROLE_FEATURE_ACTIONS
INSERT INTO role_feature_actions (role_feature_id, action)
VALUES (1, 'CREATE'),
       (1, 'READ'),
       (1, 'UPDATE'),
       (1, 'DELETE'),
       (2, 'READ'),
       (3, 'READ');

-- USERS
INSERT INTO users (id, username, first_name, last_name, password, email, enabled, locked, created_at, updated_at,
                   version)
VALUES (1, 'admin', 'Admin', 'User', '$2a$12$3.u/zYdeW169uAxqBnPnHuOdPHgjGL.I6O.E1IrZzNd3plVqOkJMi', 'admin@example.com', true, false, '2025-01-01 10:00:00',
        '2025-01-01 10:00:00', 0),
       (2, 'johndoe', 'John', 'Doe', '$2a$12$wh97ywnJFR761hMaXnk7ZuqJVDqxTQUMsvC5qPHmorcvkubE.0U2i', 'john@example.com', true, false, '2025-01-01 10:00:00',
        '2025-01-01 10:00:00', 0);

-- USER_ROLES
INSERT INTO user_roles (id, user_id, role_id, assigned_at, active, inherit_permissions, created_at, updated_at, version)
VALUES (1, 1, 1, '2025-01-01 10:00:00', true, true, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0),
       (2, 2, 2, '2025-01-01 10:00:00', true, true, '2025-01-01 10:00:00', '2025-01-01 10:00:00', 0);
