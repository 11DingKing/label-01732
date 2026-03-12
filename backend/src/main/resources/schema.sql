-- =============================================
-- 游戏卡密管理系统 - 数据库初始化脚本
-- =============================================

-- 设置字符集（确保中文正确存储）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS card_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE card_manager;

-- 设置当前连接的字符集
SET NAMES utf8mb4;

-- =============================================
-- 1. 系统用户表
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'operator' COMMENT '角色：admin/operator',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- =============================================
-- 2. 卡密批次表
-- =============================================
DROP TABLE IF EXISTS card_batch;
CREATE TABLE card_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    batch_number VARCHAR(20) NOT NULL COMMENT '批次号（YYYYMMDDHHmmss格式）',
    total_count INT NOT NULL DEFAULT 0 COMMENT '发卡数量',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已核销数量',
    recycled_count INT NOT NULL DEFAULT 0 COMMENT '已回收数量',
    operator_id BIGINT COMMENT '操作员ID',
    operator_name VARCHAR(50) COMMENT '操作员姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    UNIQUE KEY uk_batch_number (batch_number),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡密批次表';

-- =============================================
-- 3. 卡密信息表
-- =============================================
DROP TABLE IF EXISTS card_info;
CREATE TABLE card_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    card_number VARCHAR(20) NOT NULL COMMENT '卡号（9位唯一数字）',
    card_password VARCHAR(20) NOT NULL COMMENT '密码（6位随机字符）',
    batch_number VARCHAR(20) NOT NULL COMMENT '批次号（YYYYMMDDHHmmss格式，14位）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用 1-已核销 2-已回收',
    create_operator_id BIGINT COMMENT '创建操作员ID',
    create_operator_name VARCHAR(50) COMMENT '创建操作员姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    use_time DATETIME COMMENT '核销时间',
    use_operator_id BIGINT COMMENT '核销操作员ID',
    use_operator_name VARCHAR(50) COMMENT '核销操作员姓名',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除（注意：回收操作不设置此字段，仅修改status）',
    UNIQUE KEY uk_card_number (card_number),
    KEY idx_batch_number (batch_number),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    KEY idx_use_time (use_time),
    KEY idx_create_operator (create_operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡密信息表';

-- =============================================
-- 4. 操作日志表
-- =============================================
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    module VARCHAR(50) COMMENT '模块',
    operation VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    result VARCHAR(500) COMMENT '操作结果',
    operator_id BIGINT COMMENT '操作员ID',
    operator_name VARCHAR(50) COMMENT '操作员姓名',
    ip VARCHAR(50) COMMENT 'IP地址',
    cost_time BIGINT COMMENT '耗时（毫秒）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    KEY idx_operator_id (operator_id),
    KEY idx_create_time (create_time),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化管理员账号（密码：admin123）
-- 使用 BCrypt 加密，可通过 https://bcrypt-generator.com/ 在线生成验证
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$aL5aOVoARSGjOZ7H6clsbeho8Ow5yXFxVFpeG6.Ts3irdg7E1CuDa', '系统管理员', 'admin', 0);

-- 初始化测试操作员账号（密码：123456）
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('operator', '$2a$10$7pKAWbjsdChPxkEHaz11Re8tlxt/dP6rt.Rqk0znlOwXTsJbRKsq6', '测试操作员', 'operator', 0);
