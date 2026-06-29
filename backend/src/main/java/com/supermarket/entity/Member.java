package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员实体类
 */
@Data
@TableName("member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String phone;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 累计消费金额
     */
    private BigDecimal totalSpent;

    /**
     * 会员等级
     */
    private String level;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}