package com.supermarket.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.PageResult;
import com.supermarket.dto.OrderDTO;
import com.supermarket.dto.OrderQueryDTO;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import com.supermarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 订单服务实现类
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private MemberLevelLogMapper memberLevelLogMapper;

    @Override
    public Order create(OrderDTO orderDTO, Long cashierId) {
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单项不能为空");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setCashierId(cashierId);
        order.setMemberId(orderDTO.getMemberId());
        order.setPayType(orderDTO.getPayType());
        order.setRemark(orderDTO.getRemark());
        order.setStatus("PAID");

        // 获取会员等级信息（折扣、积分倍率）
        Member member = null;
        BigDecimal discountRate = BigDecimal.ONE; // 默认无折扣
        BigDecimal pointsMultiplier = BigDecimal.ONE; // 默认1倍积分

        if (orderDTO.getMemberId() != null) {
            member = memberMapper.selectById(orderDTO.getMemberId());
            if (member != null) {
                LambdaQueryWrapper<MemberLevel> lvWrapper = new LambdaQueryWrapper<>();
                lvWrapper.eq(MemberLevel::getName, member.getLevel())
                        .eq(MemberLevel::getStatus, 1);
                MemberLevel level = memberLevelMapper.selectOne(lvWrapper);
                if (level != null) {
                    discountRate = level.getDiscountRate();
                    pointsMultiplier = level.getPointsMultiplier();
                }
            }
        }

        // 计算订单总金额并扣库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("商品不存在：" + itemDTO.getProductName());
            }
            if (product.getStatus() != 1) {
                throw new IllegalArgumentException("商品已下架：" + product.getName());
            }
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("商品库存不足：" + product.getName());
            }

            // 扣减库存
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productMapper.updateById(product);

            BigDecimal subtotal = itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 累计成本（进价 × 数量）
            BigDecimal costPrice = product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
            totalCost = totalCost.add(costPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        order.setCostAmount(totalCost);

        // 会员折扣
        BigDecimal payAmount = totalAmount.multiply(discountRate).setScale(2, RoundingMode.DOWN);
        BigDecimal discountAmount = totalAmount.subtract(payAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);

        // 保存订单
        orderMapper.insert(order);

        // 保存订单项
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setProductName(itemDTO.getProductName());
            item.setPrice(itemDTO.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            item.setSubtotal(itemDTO.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItemMapper.insert(item);
        }

        // 会员权益处理
        if (member != null) {
            // 积分计算（含倍率）：每1元积0.1分 * 倍率
            int currentPoints = member.getPoints() == null ? 0 : member.getPoints();
            int basePoints = payAmount.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();
            int earnedPoints = BigDecimal.valueOf(basePoints).multiply(pointsMultiplier).setScale(0, RoundingMode.DOWN).intValue();
            member.setPoints(currentPoints + earnedPoints);

            // 累计消费
            BigDecimal newTotalSpent = (member.getTotalSpent() == null ? BigDecimal.ZERO : member.getTotalSpent()).add(payAmount);
            member.setTotalSpent(newTotalSpent);

            String oldLevel = member.getLevel();
            String newLevel = autoUpgradeLevel(newTotalSpent);
            if (!newLevel.equals(oldLevel)) {
                member.setLevel(newLevel);
                // 记录等级变更
                MemberLevelLog log = new MemberLevelLog();
                log.setMemberId(member.getId());
                log.setOldLevel(oldLevel);
                log.setNewLevel(newLevel);
                memberLevelLogMapper.insert(log);
            }

            memberMapper.updateById(member);
        }

        return order;
    }

    /**
     * 根据累计消费额自动判断会员等级
     */
    private String autoUpgradeLevel(BigDecimal totalSpent) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getStatus, 1)
                .le(MemberLevel::getMinSpent, totalSpent)
                .orderByDesc(MemberLevel::getMinSpent)
                .last("LIMIT 1");
        MemberLevel level = memberLevelMapper.selectOne(wrapper);
        return level != null ? level.getName() : "普通会员";
    }

    @Override
    public PageResult<Order> list(OrderQueryDTO queryDTO) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getStartDate())) {
            wrapper.ge(Order::getCreateTime, queryDTO.getStartDate() + " 00:00:00");
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            wrapper.le(Order::getCreateTime, queryDTO.getEndDate() + " 23:59:59");
        }
        if (queryDTO.getCashierId() != null) {
            wrapper.eq(Order::getCashierId, queryDTO.getCashierId());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getPayType())) {
            wrapper.eq(Order::getPayType, queryDTO.getPayType());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> iPage = orderMapper.selectPage(
                new Page<>(queryDTO.getPage(), queryDTO.getPageSize()), wrapper);
        return new PageResult<>(iPage.getTotal(), queryDTO.getPage(), queryDTO.getPageSize(), iPage.getRecords());
    }

    @Override
    public Order detail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    @Override
    public void refund(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalArgumentException("当前订单状态不支持退款");
        }

        order.setStatus("REFUNDING");
        orderMapper.updateById(order);

        // 恢复库存
        List<OrderItem> items = getOrderItems(id);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 会员权益回滚（扣积分、减累计消费、重新计算等级）
        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                BigDecimal payAmount = order.getPayAmount();

                // 计算退款应扣除的积分（与下单时对应）
                int currentPoints = member.getPoints() == null ? 0 : member.getPoints();
                int basePoints = payAmount.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();
                // 按基础倍率1.0扣回（实际倍率未持久化，保守扣回）
                int refundPoints = Math.min(basePoints, currentPoints); // 确保积分不为负数
                member.setPoints(currentPoints - refundPoints);

                // 扣减累计消费
                BigDecimal currentTotalSpent = member.getTotalSpent() == null ? BigDecimal.ZERO : member.getTotalSpent();
                BigDecimal newTotalSpent = currentTotalSpent.subtract(payAmount);
                if (newTotalSpent.compareTo(BigDecimal.ZERO) < 0) {
                    newTotalSpent = BigDecimal.ZERO;
                }
                String oldLevel = member.getLevel();
                String newLevel = autoUpgradeLevel(newTotalSpent);
                member.setTotalSpent(newTotalSpent);

                // 等级变化时记录日志
                if (!newLevel.equals(oldLevel)) {
                    member.setLevel(newLevel);
                    MemberLevelLog log = new MemberLevelLog();
                    log.setMemberId(member.getId());
                    log.setOldLevel(oldLevel);
                    log.setNewLevel(newLevel);
                    memberLevelLogMapper.insert(log);
                }

                memberMapper.updateById(member);
            }
        }

        order.setStatus("REFUNDED");
        orderMapper.updateById(order);
    }

    private String generateOrderNo() {
        String datePart = DateUtil.format(new Date(), "yyyyMMdd");
        int random = new Random().nextInt(999999);
        return "ORD" + datePart + String.format("%06d", random);
    }
}