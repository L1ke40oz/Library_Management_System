-- ============================================
-- 图书馆管理系统 - 数据库完整初始化脚本
-- 使用方法: mysql -u root -p密码 < init_all.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- 1. 用户表（登录/注册）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 图书表
CREATE TABLE IF NOT EXISTS book (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图书编号',
    book_name VARCHAR(500) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN',
    category VARCHAR(50) COMMENT '分类',
    description TEXT COMMENT '图书简介',
    total_count INT NOT NULL DEFAULT 0 COMMENT '馆藏数量',
    borrow_count INT NOT NULL DEFAULT 0 COMMENT '借阅数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 3. 读者表
CREATE TABLE IF NOT EXISTS reader (
    reader_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '读者编号',
    reader_name VARCHAR(50) NOT NULL COMMENT '读者姓名',
    gender VARCHAR(10) COMMENT '性别',
    class_name VARCHAR(50) COMMENT '班级',
    contact VARCHAR(50) COMMENT '联系方式',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者表';

-- 4. 管理员表
CREATE TABLE IF NOT EXISTS admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员编号',
    admin_name VARCHAR(50) NOT NULL COMMENT '管理员姓名',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    permission VARCHAR(50) DEFAULT 'NORMAL' COMMENT '权限'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 5. 借阅记录表（关联users表）
CREATE TABLE IF NOT EXISTS borrow (
    borrow_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '借阅编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书编号',
    borrow_date DATE NOT NULL COMMENT '借阅日期',
    due_date DATE NOT NULL COMMENT '应还日期',
    return_date DATE COMMENT '归还日期',
    status VARCHAR(20) DEFAULT 'BORROWED' COMMENT '状态: BORROWED/RETURNED/OVERDUE',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES book(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 6. 收藏表
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_book (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES book(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================
-- 插入图书示例数据
-- ============================================

INSERT INTO book (book_name, author, publisher, isbn, category, description, total_count, borrow_count) VALUES
-- 计算机科学
('数据结构与算法分析', '马克·艾伦·维斯', '机械工业出版社', '9787111528395', '计算机科学', '本书是数据结构和算法分析的经典教材，全面介绍了数据结构的基本概念、常用算法及其复杂度分析，适合计算机专业学生学习。', 5, 2),
('计算机网络：自顶向下方法', '詹姆斯·库罗斯', '机械工业出版社', '9787111599715', '计算机科学', '本书采用自顶向下的方法讲解计算机网络原理，从应用层开始逐步深入到物理层，内容丰富且易于理解。', 4, 3),
('深入理解计算机系统', '兰德尔·布莱恩特', '机械工业出版社', '9787111544937', '计算机科学', '从程序员的视角详细阐述计算机系统的本质概念，涵盖硬件架构、操作系统、编译链接等核心内容。', 3, 1),
('数据库系统概论', '王珊', '高等教育出版社', '9787040406641', '计算机科学', '国内数据库领域权威教材，系统讲解关系数据库理论、SQL语言、数据库设计与管理等核心知识。', 7, 3),
('操作系统概念', '亚伯拉罕·西尔伯沙茨', '机械工业出版社', '9787111604365', '计算机科学', '操作系统领域的经典教材，涵盖进程管理、内存管理、文件系统、I/O系统等操作系统核心概念。', 4, 2),
('编译原理', '阿尔弗雷德·艾侯', '机械工业出版社', '9787111269298', '计算机科学', '编译器设计领域的权威教材，被称为"龙书"，系统讲解词法分析、语法分析、代码生成与优化。', 3, 1),
('计算机组成原理', '唐朔飞', '高等教育出版社', '9787040536096', '计算机科学', '国内计算机组成原理经典教材，详细讲解CPU、存储器、总线、I/O系统等硬件组成与工作原理。', 5, 2),
('软件工程导论', '张海藩', '清华大学出版社', '9787302330981', '计算机科学', '软件工程入门教材，涵盖需求分析、系统设计、编码测试、项目管理等软件开发全生命周期。', 4, 1),
-- 编程语言
('Java核心技术 卷I', '凯·霍斯特曼', '机械工业出版社', '9787111612728', '编程语言', 'Java领域的经典著作，全面覆盖Java SE核心技术，包括面向对象、泛型、集合框架、并发编程等内容。', 6, 4),
('Python编程：从入门到实践', '埃里克·马瑟斯', '人民邮电出版社', '9787115546081', '编程语言', '零基础Python入门书籍，通过实际项目引导读者掌握Python编程，涵盖Web开发、数据可视化等实战内容。', 8, 5),
('JavaScript高级程序设计', '马特·弗里斯比', '人民邮电出版社', '9787115545381', '编程语言', '前端开发必读经典，深入讲解JavaScript语言核心机制、DOM编程、事件处理、Ajax与现代前端开发技术。', 4, 2),
('Spring Boot实战', '克雷格·沃斯', '人民邮电出版社', '9787115453686', '编程语言', '全面介绍Spring Boot框架的实战指南，帮助开发者快速构建生产级别的Spring应用程序。', 3, 1),
('C++ Primer', '斯坦利·李普曼', '电子工业出版社', '9787121155352', '编程语言', 'C++语言学习的经典教材，从基础语法到高级特性全面覆盖，适合初学者和进阶开发者。', 5, 3),
('Go语言圣经', '艾伦·多诺万', '人民邮电出版社', '9787115477316', '编程语言', 'Go语言权威入门指南，涵盖基础语法、并发编程、网络编程等核心内容，示例丰富实用。', 4, 2),
('Effective Java', '约书亚·布洛克', '机械工业出版社', '9787111612735', '编程语言', 'Java进阶必读，提供78条实践建议，帮助开发者写出更清晰、健壮、高效的Java代码。', 3, 2),
('Vue.js设计与实现', '霍春阳', '人民邮电出版社', '9787115583864', '编程语言', '深入剖析Vue.js 3框架的设计原理与实现细节，从响应式系统到编译器全面解读。', 4, 3),
-- 人工智能
('人工智能：一种现代方法', '斯图尔特·罗素', '清华大学出版社', '9787302513988', '人工智能', '人工智能领域的百科全书式教材，全面介绍AI的理论基础、核心算法和应用领域。', 3, 2),
('机器学习', '周志华', '清华大学出版社', '9787302423287', '人工智能', '周志华教授的机器学习入门经典，系统介绍机器学习的基本理论、常用算法和实践方法，被称为"西瓜书"。', 5, 4),
('深度学习', '伊恩·古德费洛', '人民邮电出版社', '9787115461476', '人工智能', '深度学习领域的奠基性著作，由三位深度学习先驱撰写，全面介绍深度学习的数学基础和核心技术。', 4, 3),
('统计学习方法', '李航', '清华大学出版社', '9787302517276', '人工智能', '统计学习领域的经典教材，系统介绍感知机、SVM、决策树、EM算法、HMM等核心算法。', 4, 2),
('动手学深度学习', '阿斯顿·张', '人民邮电出版社', '9787115571502', '人工智能', '结合代码实践的深度学习教材，基于PyTorch框架，涵盖CNN、RNN、注意力机制、GAN等内容。', 5, 3),
('自然语言处理入门', '何晗', '人民邮电出版社', '9787115519986', '人工智能', '中文NLP实战入门书，涵盖分词、命名实体识别、情感分析、机器翻译等核心任务。', 3, 1),
-- 文学
('红楼梦', '曹雪芹', '人民文学出版社', '9787020002207', '文学', '中国古典文学巅峰之作，以贾宝玉、林黛玉的爱情悲剧为主线，展现封建社会的兴衰历程。', 10, 6),
('百年孤独', '加西亚·马尔克斯', '南海出版公司', '9787544253994', '文学', '魔幻现实主义文学代表作，讲述布恩迪亚家族七代人的传奇故事，反映拉丁美洲百年沧桑。', 6, 4),
('活着', '余华', '作家出版社', '9787506365437', '文学', '余华代表作，以简洁有力的笔触讲述主人公福贵历经苦难却依然坚韧活着的人生故事。', 8, 5),
('围城', '钱钟书', '人民文学出版社', '9787020024759', '文学', '钱钟书先生的讽刺小说杰作，以幽默犀利的笔法描绘了抗战时期知识分子的生活百态。', 5, 2),
('平凡的世界', '路遥', '北京十月文艺出版社', '9787530216781', '文学', '全景式展现中国当代城乡社会生活的长篇小说，描写了孙少安、孙少平兄弟的奋斗历程。', 7, 4),
('白鹿原', '陈忠实', '人民文学出版社', '9787020090006', '文学', '以白鹿原上白、鹿两家的命运纠葛为线索，展现了关中平原半个多世纪的历史变迁。', 4, 2),
('挪威的森林', '村上春树', '上海译文出版社', '9787532725694', '文学', '村上春树代表作，以细腻的笔触描写青春期的迷惘与成长，充满对生命与爱情的思考。', 5, 3),
('人间失格', '太宰治', '作家出版社', '9787506394864', '文学', '太宰治的自传体小说，以主人公大庭叶藏的视角展现了一个敏感灵魂在社会中的挣扎与沉沦。', 4, 2),
('月亮与六便士', '毛姆', '上海译文出版社', '9787532776399', '文学', '以画家高更为原型，讲述一个中年人抛弃一切追寻艺术理想的故事，探讨理想与现实的冲突。', 5, 3),
-- 科幻
('三体', '刘慈欣', '重庆出版社', '9787229042066', '科幻', '中国科幻里程碑式作品，讲述地球文明与三体文明之间的史诗级对抗，想象宏大、逻辑严密。', 10, 8),
('银河帝国：基地', '艾萨克·阿西莫夫', '江苏凤凰文艺出版社', '9787539944562', '科幻', '科幻文学史上的不朽经典，讲述银河帝国衰落时期数学家谢顿建立基地拯救文明的宏大故事。', 5, 3),
('流浪地球', '刘慈欣', '中国华侨出版社', '9787511371164', '科幻', '刘慈欣中短篇科幻代表作合集，包含同名电影原著，展现人类面对末日危机时的勇气与智慧。', 6, 4),
('神经漫游者', '威廉·吉布森', '江苏凤凰文艺出版社', '9787559414175', '科幻', '赛博朋克文学开山之作，描绘了一个由跨国公司统治、黑客纵横的高科技低生活未来世界。', 3, 1),
-- 数学
('高等数学（上册）', '同济大学数学系', '高等教育出版社', '9787040396638', '数学', '高等院校通用数学教材，系统讲解微积分的基本理论和方法，是理工科学生的必修课程用书。', 12, 7),
('线性代数', '同济大学数学系', '高等教育出版社', '9787040396621', '数学', '线性代数基础教材，涵盖行列式、矩阵、向量空间、线性变换、特征值等核心内容。', 10, 5),
('概率论与数理统计', '浙江大学', '高等教育出版社', '9787040238969', '数学', '概率论与数理统计经典教材，系统介绍概率论基础、随机变量、统计推断等内容。', 9, 4),
('离散数学及其应用', '肯尼斯·罗森', '机械工业出版社', '9787111552475', '数学', '离散数学领域的经典教材，涵盖逻辑、集合、图论、组合数学、代数结构等内容。', 5, 2),
('数学分析', '华东师范大学', '高等教育出版社', '9787040295672', '数学', '数学专业核心教材，比高等数学更深入，系统讲解实数理论、极限、微积分的严格证明。', 4, 1),
('具体数学', '高德纳', '人民邮电出版社', '9787115300812', '数学', '计算机科学的数学基础，涵盖求和、递归、整数函数、数论、生成函数等内容，兼具趣味与深度。', 3, 1);

-- ============================================
-- 7. 图书标签表
-- ============================================

CREATE TABLE IF NOT EXISTS book_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    FOREIGN KEY (book_id) REFERENCES book(book_id),
    INDEX idx_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书标签表';

-- 8. 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES book(book_id),
    INDEX idx_book (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书评论表';

-- ============================================
-- 插入标签数据
-- ============================================

-- 新书速递
INSERT INTO book_tag (book_id, tag) VALUES
(16, '新书速递'), (21, '新书速递'), (22, '新书速递'),
(14, '新书速递'), (20, '新书速递'), (34, '新书速递');

-- 文学经典
INSERT INTO book_tag (book_id, tag) VALUES
(23, '文学经典'), (24, '文学经典'), (25, '文学经典'),
(26, '文学经典'), (27, '文学经典'), (28, '文学经典'), (31, '文学经典');

-- 考试专区
INSERT INTO book_tag (book_id, tag) VALUES
(1, '考试专区'), (4, '考试专区'), (5, '考试专区'), (7, '考试专区'),
(36, '考试专区'), (37, '考试专区'), (38, '考试专区'), (39, '考试专区');

-- 馆藏排行
INSERT INTO book_tag (book_id, tag) VALUES
(32, '馆藏排行'), (36, '馆藏排行'), (23, '馆藏排行'),
(10, '馆藏排行'), (25, '馆藏排行'), (37, '馆藏排行'), (18, '馆藏排行');
