-- =============================================
-- 测试环境数据库脚本 (H2 兼容)
-- =============================================

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'operator',
    status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_username UNIQUE (username)
);

-- 卡密批次表
CREATE TABLE IF NOT EXISTS card_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_number VARCHAR(20) NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    used_count INT NOT NULL DEFAULT 0,
    recycled_count INT NOT NULL DEFAULT 0,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_batch_number UNIQUE (batch_number)
);

-- 卡密信息表
-- 注意：is_deleted 用于真正的删除操作，回收操作仅修改 status 字段
CREATE TABLE IF NOT EXISTS card_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(20) NOT NULL,
    card_password VARCHAR(20) NOT NULL,
    batch_number VARCHAR(20) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,  -- 0:未使用 1:已核销 2:已回收
    create_operator_id BIGINT,
    create_operator_name VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    use_time TIMESTAMP,
    use_operator_id BIGINT,
    use_operator_name VARCHAR(50),
    is_deleted TINYINT NOT NULL DEFAULT 0,  -- 仅用于真正删除，回收不设置此字段
    CONSTRAINT uk_card_number UNIQUE (card_number)
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(50),
    operation VARCHAR(50),
    method VARCHAR(200),
    params CLOB,
    result VARCHAR(500),
    operator_id BIGINT,
    operator_name VARCHAR(50),
    ip VARCHAR(50),
    cost_time BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初始化测试数据
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfPzXZ/IJXM25P0FqWbeK', '管理员', 'admin', 0),
('operator', '$2a$10$Dg1GR/BWBc4.dCqC3rVPV.cXUi7fJNQlLZN8xGIj5h1EqHBvQ7BHm', '操作员', 'operator', 0);
