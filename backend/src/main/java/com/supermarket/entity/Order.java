package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠/折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 成本金额（根据商品进价计算）
     */
    private BigDecimal costAmount;

    /**
     * 支付方式：CASH-现金，ALIPAY-支付宝，WECHAT-微信
     */
    private String payType;

    /**
     * 订单状态：PENDING-待支付，PAID-已支付，REFUNDING-退款中，REFUNDED-已退款，CANCELED-已取消
     */
    private String status;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 收银员ID
     */
    private Long cashierId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}