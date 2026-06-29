package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员等级配置
 */
@Data
@TableName("member_level")
public class MemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 等级名称 */
    private String name;

    /** 升级门槛（累计消费达到此金额自动升级） */
    private BigDecimal minSpent;

    /** 折扣比例（如0.95表示95折） */
    private BigDecimal discountRate;

    /** 积分倍率（如1.5表示1.5倍积分） */
    private BigDecimal pointsMultiplier;

    /** 排序序号 */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}