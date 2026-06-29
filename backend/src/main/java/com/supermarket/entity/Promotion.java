package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("promotion")
public class Promotion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private Date startTime;

    private Date endTime;

    private String ruleConfig;

    private String remark;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}