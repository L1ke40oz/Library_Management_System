-- ===== 管理员功能迁移脚本 =====
-- 如果你的数据库已经存在，请执行以下SQL来添加管理员功能所需的表结构变更

USE library_db;

-- 1. 给 book 表添加 description 字段（如果不存在）
ALTER TABLE book ADD COLUMN IF NOT EXISTS description TEXT COMMENT '简介' AFTER category;

-- 2. 创建图书标签表
CREATE TABLE IF NOT EXISTS book_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '图书编号',
    tag VARCHAR(50) NOT NULL COMMENT '标签',
    FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书标签表';

-- 3. 创建站内消息表
CREATE TABLE IF NOT EXISTS message (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息编号',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(30) DEFAULT 'SYSTEM' COMMENT '消息类型: SYSTEM/OVERDUE/NOTICE',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 4. 插入默认管理员账号（用户名: admin, 密码: admin123）
INSERT IGNORE INTO users(username, email, password, role)
VALUES('admin', 'admin@library.com', 'admin123', 'ADMIN');
