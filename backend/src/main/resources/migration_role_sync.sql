-- ===== 角色子表关联迁移脚本 =====
-- 为 reader 和 admin 表添加 user_id 字段，关联 users 表

USE library_db;

-- 1. 给 reader 表添加 user_id 字段
ALTER TABLE reader ADD COLUMN user_id BIGINT UNIQUE COMMENT '关联users表的用户ID';
ALTER TABLE reader ADD CONSTRAINT fk_reader_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 2. 给 admin 表添加 user_id 字段
ALTER TABLE admin ADD COLUMN user_id BIGINT UNIQUE COMMENT '关联users表的用户ID';
ALTER TABLE admin ADD CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 3. 将现有 users 表中 role='USER' 的用户同步到 reader 表
INSERT INTO reader (reader_name, contact, status, user_id)
SELECT username, email, 'ACTIVE', id
FROM users
WHERE role = 'USER'
AND id NOT IN (SELECT user_id FROM reader WHERE user_id IS NOT NULL);

-- 4. 将现有 users 表中 role='ADMIN' 的用户同步到 admin 表
INSERT INTO admin (admin_name, username, password, permission, user_id)
SELECT username, username, password, 'NORMAL', id
FROM users
WHERE role = 'ADMIN'
AND id NOT IN (SELECT user_id FROM admin WHERE user_id IS NOT NULL);
