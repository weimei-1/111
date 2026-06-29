package com.supermarket.controller;

import com.supermarket.common.PageResult;
import com.supermarket.common.Result;
import com.supermarket.dto.OrderDTO;
import com.supermarket.dto.OrderQueryDTO;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Order> create(@RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        Long cashierId = (Long) request.getAttribute("userId");
        Order order = orderService.create(orderDTO, cashierId);
        return Result.success("创建成功", order);
    }

    /**
     * 分页查询订单
     */
    @GetMapping("/list")
    public Result<PageResult<Order>> list(OrderQueryDTO queryDTO) {
        return Result.success(orderService.list(queryDTO));
    }

    /**
     * 订单详情（包含订单项）
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Order order = orderService.detail(id);
        List<OrderItem> items = orderService.getOrderItems(id);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return Result.success(result);
    }

    /**
     * 修改订单状态
     */
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return Result.success("修改成功", null);
    }

    /**
     * 退款
     */
    @PostMapping("/refund/{id}")
    public Result<?> refund(@PathVariable Long id) {
        orderService.refund(id);
        return Result.success("退款成功", null);
    }

    /**
     * 打印小票（返回格式化文本）
     */
    @GetMapping("/print/{id}")
    public Result<Map<String, Object>> print(@PathVariable Long id) {
        Order order = orderService.detail(id);
        List<OrderItem> items = orderService.getOrderItems(id);

        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append("         超市购物小票\n");
        sb.append("================================================\n");
        sb.append("订单编号：").append(order.getOrderNo()).append("\n");
        sb.append("支付方式：").append(order.getPayType()).append("\n");
        sb.append("--------------------------------\n");
        sb.append("商品名称\t单价\t数量\t小计\n");
        sb.append("--------------------------------\n");
        for (OrderItem item : items) {
            sb.append(item.getProductName()).append("\t")
                    .append(item.getPrice()).append("\t")
                    .append(item.getQuantity()).append("\t")
                    .append(item.getSubtotal()).append("\n");
        }
        sb.append("--------------------------------\n");
        sb.append("总金额：").append(order.getTotalAmount()).append("\n");
        sb.append("优惠金额：").append(order.getDiscountAmount()).append("\n");
        sb.append("实付金额：").append(order.getPayAmount()).append("\n");
        sb.append("================================================\n");

        Map<String, Object> result = new HashMap<>();
        result.put("printContent", sb.toString());
        result.put("order", order);
        result.put("items", items);
        return Result.success(result);
    }
}