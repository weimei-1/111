package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userName;

    private String module;

    private String action;

    private Long targetId;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}