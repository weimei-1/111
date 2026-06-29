package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 商品条码
     */
    private String barcode;

    private Long categoryId;

    /**
     * 零售价
     */
    private BigDecimal price;

    /**
     * 进价/成本价
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 库存预警数量
     */
    private Integer warningStock;

    /**
     * 单位（个、斤、箱等）
     */
    private String unit;

    /**
     * 商品图片URL
     */
    private String image;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}