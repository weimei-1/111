-- ========================================
-- 超市订单管理系统 - 数据库迁移脚本
-- 执行日期：2025-06-29
-- 说明：为已有数据库添加新表和字段，不影响已有数据
-- ========================================

-- 1. 订单表新增成本金额字段
ALTER TABLE orders 
ADD COLUMN cost_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本金额（根据商品进价计算）'
AFTER pay_amount;

-- 2. 会员表新增累计消费金额字段
ALTER TABLE member 
ADD COLUMN total_spent DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费金额'
AFTER points;

-- 3. 创建会员等级配置表
CREATE TABLE IF NOT EXISTS member_level (
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

-- 4. 创建等级变更记录表
CREATE TABLE IF NOT EXISTS member_level_log (
    id BIGINT AUTO_INCREMENT COMMENT '记录ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    old_level VARCHAR(50) NOT NULL COMMENT '旧等级',
    new_level VARCHAR(50) NOT NULL COMMENT '新等级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级变更记录表';

-- 6. 创建供应商表
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT COMMENT '供应商ID',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) DEFAULT '' COMMENT '联系人',
    phone VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    address VARCHAR(255) DEFAULT '' COMMENT '地址',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 7. 创建采购订单表
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT COMMENT '采购单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '采购单号',
    supplier_id BIGINT DEFAULT NULL COMMENT '供应商ID',
    total_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '采购总金额',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    status INT DEFAULT 0 COMMENT '状态：0-待入库，1-已入库，2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_no (order_no),
    KEY idx_supplier_id (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- 8. 创建采购订单明细表
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT COMMENT '明细ID',
    purchase_order_id BIGINT NOT NULL COMMENT '采购单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) DEFAULT '' COMMENT '商品名称',
    quantity INT NOT NULL DEFAULT 0 COMMENT '采购数量',
    purchase_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '采购单价',
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
    PRIMARY KEY (id),
    KEY idx_purchase_order_id (purchase_order_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- 9. 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    user_name VARCHAR(50) DEFAULT '' COMMENT '操作用户名',
    module VARCHAR(50) NOT NULL COMMENT '操作模块',
    action VARCHAR(50) NOT NULL COMMENT '操作类型（add/update/delete/refund等）',
    target_id BIGINT DEFAULT NULL COMMENT '操作对象ID',
    detail VARCHAR(500) DEFAULT '' COMMENT '操作详情',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_module (module),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 10. 创建促销活动表
CREATE TABLE IF NOT EXISTS promotion (
    id BIGINT AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    type VARCHAR(50) NOT NULL COMMENT '活动类型：full_discount-满减, discount-折扣, member_discount-会员特价',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    rule_config TEXT COMMENT '规则配置JSON',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    status INT DEFAULT 0 COMMENT '状态：0-未启用，1-已启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

-- 5. 插入默认会员等级（如果表为空）
INSERT INTO member_level (name, min_spent, discount_rate, points_multiplier, sort, status)
SELECT * FROM (
    SELECT '普通会员' AS name, 0 AS min_spent, 1.00 AS discount_rate, 1.0 AS points_multiplier, 1 AS sort, 1 AS status
    UNION SELECT '银卡会员', 500, 0.95, 1.2, 2, 1
    UNION SELECT '金卡会员', 2000, 0.90, 1.5, 3, 1
    UNION SELECT '钻石会员', 5000, 0.85, 2.0, 4, 1
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM member_level LIMIT 1);