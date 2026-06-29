package com.supermarket.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
public class OrderDTO {

    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;

    /**
     * 会员ID（可选）
     */
    private Long memberId;

    /**
     * 支付方式：CASH-现金，ALIPAY-支付宝，WECHAT-微信
     */
    private String payType;

    /**
     * 备注
     */
    private String remark;

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 订单项内部类
     */
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}