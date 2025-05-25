-- Initial data for the RBAC (Role-Based Access Control) system

-- Admin user (password: admin123)
INSERT INTO users (id, username, first_name, last_name, password, email, system_account, created_at, created_by)
VALUES (1, 'admin', 'System', 'Administrator',
        '$2a$12$.Na/CIFwozKPgoW5rcUzH.0dN0TDOS/Jktl2JmhoX8mnAT4K2eSYa',
        'admin@example.com', true, CURRENT_TIMESTAMP, 'system') ON CONFLICT (id) DO NOTHING;

-- Basic user (password: user123)
INSERT INTO users (id, username, first_name, last_name, password, email, created_at, created_by)
VALUES (2, 'user', 'Basic', 'User',
        '$2a$12$UzTWzW0a7XtyXIlPdvnFJO9Rx0s2FZajuU3Ppg4BWN2UAGwK2bMdG',
        'user@example.com', CURRENT_TIMESTAMP, 'system') ON CONFLICT (id) DO NOTHING;

-- Core features
INSERT INTO features (id, name, code, description, enabled, display_order, created_at, created_by)
VALUES (1, 'User Management', 'users', 'Manage system users', true, 10, CURRENT_TIMESTAMP, 'system'),
       (2, 'Role Management', 'roles', 'Manage roles and permissions', true, 20, CURRENT_TIMESTAMP, 'system'),
       (3, 'Feature Management', 'features', 'Manage system features', true, 30, CURRENT_TIMESTAMP, 'system'),
       (4, 'Permission Management', 'permissions', 'Manage permissions', true, 40, CURRENT_TIMESTAMP, 'system'),
       (5, 'System Settings', 'settings', 'Manage system settings', true, 50, CURRENT_TIMESTAMP,
        'system') ON CONFLICT (id) DO NOTHING;

-- User Management sub-features
INSERT INTO features (id, name, code, description, enabled, display_order, parent_id, created_at, created_by)
VALUES (101, 'User Profiles', 'user_profiles', 'Manage user profiles', true, 11, 1, CURRENT_TIMESTAMP, 'system'),
       (102, 'User Roles', 'user_roles', 'Manage user role assignments', true, 12, 1, CURRENT_TIMESTAMP,
        'system') ON CONFLICT (id) DO NOTHING;

-- Permissions
INSERT INTO permissions (id, feature_id, action, description, created_at, created_by)
VALUES (1, 1, 'VIEW', 'View users list', CURRENT_TIMESTAMP, 'system'),
       (2, 1, 'CREATE', 'Create new users', CURRENT_TIMESTAMP, 'system'),
       (3, 1, 'UPDATE', 'Update existing users', CURRENT_TIMESTAMP, 'system'),
       (4, 1, 'DELETE', 'Delete users', CURRENT_TIMESTAMP, 'system'),
       (5, 1, 'ASSIGN_ROLE', 'Assign roles to users', CURRENT_TIMESTAMP, 'system'),
       (6, 1, 'REMOVE_ROLE', 'Remove roles from users', CURRENT_TIMESTAMP, 'system'),
       (7, 1, 'RESET_PASSWORD', 'Reset user passwords', CURRENT_TIMESTAMP, 'system'),
       (8, 2, 'VIEW', 'View roles list', CURRENT_TIMESTAMP, 'system'),
       (9, 2, 'CREATE', 'Create new roles', CURRENT_TIMESTAMP, 'system'),
       (10, 2, 'UPDATE', 'Update existing roles', CURRENT_TIMESTAMP, 'system'),
       (11, 2, 'DELETE', 'Delete roles', CURRENT_TIMESTAMP, 'system'),
       (12, 2, 'ASSIGN_PERMISSION', 'Assign permissions to roles', CURRENT_TIMESTAMP, 'system'),
       (13, 3, 'VIEW', 'View features list', CURRENT_TIMESTAMP, 'system'),
       (14, 3, 'CREATE', 'Create new features', CURRENT_TIMESTAMP, 'system'),
       (15, 3, 'UPDATE', 'Update existing features', CURRENT_TIMESTAMP, 'system'),
       (16, 3, 'DELETE', 'Delete features', CURRENT_TIMESTAMP, 'system'),
       (17, 4, 'VIEW', 'View permissions list', CURRENT_TIMESTAMP, 'system'),
       (18, 4, 'CREATE', 'Create new permissions', CURRENT_TIMESTAMP, 'system'),
       (19, 4, 'UPDATE', 'Update existing permissions', CURRENT_TIMESTAMP, 'system'),
       (20, 4, 'DELETE', 'Delete permissions', CURRENT_TIMESTAMP, 'system'),
       (21, 5, 'VIEW', 'View system settings', CURRENT_TIMESTAMP, 'system'),
       (22, 5, 'UPDATE', 'Update system settings', CURRENT_TIMESTAMP, 'system') ON CONFLICT (id) DO NOTHING;

-- Roles
INSERT INTO roles (id, name, code, description, priority, system_role, created_at, created_by)
VALUES (1, 'System Administrator', 'admin', 'Full access to all system features', 100, true, CURRENT_TIMESTAMP,
        'system'),
       (2, 'User Administrator', 'user_admin', 'Manage users and their roles', 90, true, CURRENT_TIMESTAMP, 'system'),
       (3, 'Role Administrator', 'role_admin', 'Manage roles and permissions', 80, true, CURRENT_TIMESTAMP, 'system'),
       (4, 'Basic User', 'user', 'Standard user with basic permissions', 10, true, CURRENT_TIMESTAMP,
        'system') ON CONFLICT (id) DO NOTHING;

-- Role hierarchy
UPDATE roles
SET parent_id = 1
WHERE id IN (2, 3);

-- Role permissions - Admin
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),
       (1, 15),
       (1, 16),
       (1, 17),
       (1, 18),
       (1, 19),
       (1, 20),
       (1, 21),
       (1, 22) ON CONFLICT DO NOTHING;

-- Role permissions - User Administrator
INSERT INTO role_permissions (role_id, permission_id)
VALUES (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (2, 6),
       (2, 7),
       (2, 8) ON CONFLICT DO NOTHING;

-- Role permissions - Role Administrator
INSERT INTO role_permissions (role_id, permission_id)
VALUES (3, 8),
       (3, 9),
       (3, 10),
       (3, 11),
       (3, 12),
       (3, 13),
       (3, 14),
       (3, 15),
       (3, 16),
       (3, 17),
       (3, 18),
       (3, 19),
       (3, 20) ON CONFLICT DO NOTHING;

-- Role permissions - Basic User
INSERT INTO role_permissions (role_id, permission_id)
VALUES (4, 1) ON CONFLICT DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id, assigned_at, valid_from, status, active, created_at, created_by)
VALUES (1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', true, CURRENT_TIMESTAMP, 'system'),
       (2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', true, CURRENT_TIMESTAMP,
        'system') ON CONFLICT (user_id, role_id) DO NOTHING;


-- Reset sequences to avoid duplicate key violations
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('features', 'id'), COALESCE((SELECT MAX(id) FROM features), 1), true);
SELECT setval(pg_get_serial_sequence('permissions', 'id'), COALESCE((SELECT MAX(id) FROM permissions), 1), true);
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE((SELECT MAX(id) FROM roles), 1), true);
