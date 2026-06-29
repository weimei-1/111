package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.dto.OrderDTO;
import com.supermarket.dto.OrderQueryDTO;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单（含事务、扣库存）
     *
     * @param orderDTO  订单DTO
     * @param cashierId 收银员ID
     * @return 创建的订单
     */
    Order create(OrderDTO orderDTO, Long cashierId);

    /**
     * 分页查询订单（多维度查询）
     */
    PageResult<Order> list(OrderQueryDTO queryDTO);

    /**
     * 查询订单详情
     *
     * @param id 订单ID
     * @return 包含订单项列表的订单
     */
    Order detail(Long id);

    /**
     * 获取订单项列表
     */
    List<OrderItem> getOrderItems(Long orderId);

    /**
     * 修改订单状态
     */
    void updateStatus(Long id, String status);

    /**
     * 退款
     */
    void refund(Long id);
}