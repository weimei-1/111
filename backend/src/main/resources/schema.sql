-- =============================================
-- 超市订单管理系统 - 数据库初始化脚本
-- 数据库：supermarket_db
-- =============================================

CREATE DATABASE IF NOT EXISTS supermarket_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE supermarket_db;

-- -----------------------------------------
-- 1. 用户表
-- -----------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(MD5加密)',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'CASHIER' COMMENT '角色：ADMIN-管理员，CASHIER-收银员',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -----------------------------------------
-- 2. 商品分类表
-- -----------------------------------------
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序序号',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- -----------------------------------------
-- 3. 商品表
-- -----------------------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    barcode VARCHAR(50) DEFAULT NULL COMMENT '商品条码',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '零售价',
    cost_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '进价/成本价',
    stock INT DEFAULT 0 COMMENT '库存数量',
    warning_stock INT DEFAULT 10 COMMENT '库存预警数量',
    unit VARCHAR(20) DEFAULT '个' COMMENT '单位',
    image VARCHAR(500) DEFAULT NULL COMMENT '商品图片URL',
    status INT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_barcode (barcode),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- -----------------------------------------
-- 4. 订单表
-- -----------------------------------------
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(30) NOT NULL COMMENT '订单编号',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    pay_type VARCHAR(20) DEFAULT NULL COMMENT '支付方式：CASH-现金，ALIPAY-支付宝，WECHAT-微信',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待支付，PAID-已支付，REFUNDING-退款中，REFUNDED-已退款，CANCELED-已取消',
    member_id BIGINT DEFAULT NULL COMMENT '会员ID',
    cashier_id BIGINT DEFAULT NULL COMMENT '收银员ID',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_cashier_id (cashier_id),
    KEY idx_create_time (create_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- -----------------------------------------
-- 5. 订单明细表
-- -----------------------------------------
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ==================== 数据迁移（已有数据库执行） ====================
-- 如已有 member 表，执行以下命令添加累计消费字段
-- ALTER TABLE member ADD COLUMN total_spent DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费金额';
-- 如已有数据库，执行以下命令创建新表
-- DROP TABLE IF EXISTS member_level;
-- CREATE TABLE member_level (...);
-- DROP TABLE IF EXISTS member_level_log;
-- CREATE TABLE member_level_log (...);

-- -----------------------------------------
-- 6. 会员表
-- -----------------------------------------
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT COMMENT '会员ID',
    name VARCHAR(50) NOT NULL COMMENT '会员姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    points INT DEFAULT 0 COMMENT '积分',
    total_spent DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费金额',
    level VARCHAR(20) DEFAULT '普通会员' COMMENT '会员等级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- -----------------------------------------
-- 7. 会员等级配置表
-- -----------------------------------------
DROP TABLE IF EXISTS member_level;
CREATE TABLE member_level (
    id BIGINT AUTO_INCREMENT COMMENT '等级ID',
    name VARCHAR(50) NOT NULL COMMENT '等级名称',
    min_spent DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '升级门槛(累计消费)',
    discount_rate DECIMAL(4,2) NOT NULL DEFAULT 1.00 COMMENT '折扣比例(0.95=95折)',
    points_multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.00 COMMENT '积分倍率',
    sort INT DEFAULT 0 COMMENT '排序序号',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级配置表';

-- -----------------------------------------
-- 8. 等级变更记录表
-- -----------------------------------------
DROP TABLE IF EXISTS member_level_log;
CREATE TABLE member_level_log (
    id BIGINT AUTO_INCREMENT COMMENT '记录ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    old_level VARCHAR(50) NOT NULL COMMENT '旧等级',
    new_level VARCHAR(50) NOT NULL COMMENT '新等级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级变更记录表';

-- =============================================
-- 初始数据
-- =============================================

-- 初始管理员（密码：admin123，MD5加密）
INSERT INTO sys_user (username, password, real_name, role, phone, status) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 'ADMIN', '13800000000', 1),
('cashier1', '0192023a7bbd73250516f069df18b500', '收银员张三', 'CASHIER', '13800000001', 1),
('cashier2', '0192023a7bbd73250516f069df18b500', '收银员李四', 'CASHIER', '13800000002', 1);

-- 初始商品分类
INSERT INTO category (name, sort, status) VALUES
('饮料', 1, 1),
('零食', 2, 1),
('生鲜', 3, 1),
('日用品', 4, 1),
('粮油调味', 5, 1),
('酒类', 6, 1);

-- 初始商品
INSERT INTO product (name, barcode, category_id, price, cost_price, stock, warning_stock, unit, status) VALUES
('农夫山泉 550ml', '6901010101001', 1, 2.00, 1.20, 500, 50, '瓶', 1),
('可口可乐 330ml', '6901010101002', 1, 3.00, 1.80, 300, 30, '罐', 1),
('乐事薯片 75g', '6901010101003', 2, 7.00, 4.50, 200, 20, '袋', 1),
('奥利奥饼干 97g', '6901010101004', 2, 9.00, 5.50, 150, 15, '袋', 1),
('鲜鸡蛋 10枚装', '6901010101005', 3, 15.00, 10.00, 80, 10, '盒', 1),
('蒙牛纯牛奶 250ml', '6901010101006', 1, 3.50, 2.20, 400, 40, '盒', 1),
('康师傅方便面 5包', '6901010101007', 2, 12.00, 7.50, 120, 20, '袋', 1),
('海天酱油 500ml', '6901010101008', 5, 8.00, 5.00, 100, 15, '瓶', 1),
('青岛啤酒 330ml', '6901010101009', 6, 5.00, 3.00, 250, 30, '罐', 1),
('维达纸巾 3层', '6901010101010', 4, 25.00, 16.00, 60, 10, '提', 1);

-- 初始会员
INSERT INTO member (name, phone, points, level) VALUES
('王小明', '13900000001', 500, '金卡会员'),
('赵小红', '13900000002', 200, '普通会员'),
('刘大伟', '13900000003', 1000, '钻石会员');

-- 初始会员等级配置
INSERT INTO member_level (name, min_spent, discount_rate, points_multiplier, sort, status) VALUES
('普通会员', 0, 1.00, 1.0, 1, 1),
('银卡会员', 500, 0.95, 1.2, 2, 1),
('金卡会员', 2000, 0.90, 1.5, 3, 1),
('钻石会员', 5000, 0.85, 2.0, 4, 1);