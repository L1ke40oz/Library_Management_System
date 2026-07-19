USE library_db;

-- 检查并添加 user_id 字段（如果已存在则忽略错误）
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='library_db' AND TABLE_NAME='reader' AND COLUMN_NAME='user_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE reader ADD COLUMN user_id BIGINT UNIQUE COMMENT ''关联users表的用户ID''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='library_db' AND TABLE_NAME='admin' AND COLUMN_NAME='user_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE admin ADD COLUMN user_id BIGINT UNIQUE COMMENT ''关联users表的用户ID''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加外键（如果不存在）
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA='library_db' AND TABLE_NAME='reader' AND CONSTRAINT_NAME='fk_reader_user');
SET @sql = IF(@fk_exists = 0, 'ALTER TABLE reader ADD CONSTRAINT fk_reader_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA='library_db' AND TABLE_NAME='admin' AND CONSTRAINT_NAME='fk_admin_user');
SET @sql = IF(@fk_exists = 0, 'ALTER TABLE admin ADD CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 同步现有数据
INSERT INTO reader (reader_name, contact, status, user_id)
SELECT username, email, 'ACTIVE', id
FROM users
WHERE role = 'USER'
AND id NOT IN (SELECT user_id FROM reader WHERE user_id IS NOT NULL);

INSERT INTO admin (admin_name, username, password, permission, user_id)
SELECT username, username, password, 'NORMAL', id
FROM users
WHERE role = 'ADMIN'
AND id NOT IN (SELECT user_id FROM admin WHERE user_id IS NOT NULL);
