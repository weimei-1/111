package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("inventory_count_item")
public class InventoryCountItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long countId;

    private Long productId;

    private String productName;

    private Integer bookStock;

    private Integer actualStock;

    private Integer difference;

    private String remark;
}