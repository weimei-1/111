package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 会员等级变更记录
 */
@Data
@TableName("member_level_log")
public class MemberLevelLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员ID */
    private Long memberId;

    /** 旧等级 */
    private String oldLevel;

    /** 新等级 */
    private String newLevel;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}