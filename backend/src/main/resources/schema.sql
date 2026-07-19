-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- 用户表（用于登录/注册）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图书表
CREATE TABLE IF NOT EXISTS book (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图书编号',
    book_name VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN',
    category VARCHAR(50) COMMENT '分类',
    description TEXT COMMENT '简介',
    total_count INT NOT NULL DEFAULT 0 COMMENT '馆藏数量',
    borrow_count INT NOT NULL DEFAULT 0 COMMENT '借阅数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 图书标签表
CREATE TABLE IF NOT EXISTS book_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '图书编号',
    tag VARCHAR(50) NOT NULL COMMENT '标签',
    FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书标签表';

-- 读者表
CREATE TABLE IF NOT EXISTS reader (
    reader_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '读者编号',
    reader_name VARCHAR(50) NOT NULL COMMENT '读者姓名',
    gender VARCHAR(10) COMMENT '性别',
    class_name VARCHAR(50) COMMENT '班级',
    contact VARCHAR(50) COMMENT '联系方式',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者表';

-- 管理员表（旧表，保留兼容）
CREATE TABLE IF NOT EXISTS admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员编号',
    admin_name VARCHAR(50) NOT NULL COMMENT '管理员姓名',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    permission VARCHAR(50) DEFAULT 'NORMAL' COMMENT '权限'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ===== 插入默认管理员账号（用户名: admin, 密码: admin123） =====
INSERT IGNORE INTO users(username, email, password, role)
VALUES('admin', 'admin@library.com', 'admin123', 'ADMIN');

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrow (
    borrow_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '借阅编号',
    user_id BIGINT NOT NULL COMMENT '用户编号',
    book_id BIGINT NOT NULL COMMENT '图书编号',
    borrow_date DATE NOT NULL COMMENT '借阅日期',
    due_date DATE NOT NULL COMMENT '应还日期',
    return_date DATE COMMENT '归还日期',
    status VARCHAR(20) DEFAULT 'BORROWED' COMMENT '状态: BORROWED/RETURNED/OVERDUE',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES book(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 站内消息表
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
