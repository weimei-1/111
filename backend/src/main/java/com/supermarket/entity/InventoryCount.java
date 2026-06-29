package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("inventory_count")
public class InventoryCount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String countNo;

    private Date countDate;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(exist = false)
    private List<InventoryCountItem> items;
}