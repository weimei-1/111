package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单 Mapper 接口
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 多条件查询订单
     */
    List<Order> selectByConditions(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   @Param("cashierId") Long cashierId,
                                   @Param("productName") String productName,
                                   @Param("status") String status);

    /**
     * 查询指定日期的营业额
     */
    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM orders WHERE status = 'PAID' AND DATE(create_time) = #{date}")
    BigDecimal getDailyRevenue(@Param("date") String date);

    /**
     * 查询指定日期的订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE status = 'PAID' AND DATE(create_time) = #{date}")
    Long getDailyOrderCount(@Param("date") String date);

    /**
     * 查询指定月份的营业额
     */
    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM orders WHERE status = 'PAID' AND DATE_FORMAT(create_time, '%Y-%m') = #{yearMonth}")
    BigDecimal getMonthlyRevenue(@Param("yearMonth") String yearMonth);

    /**
     * 查询指定日期的总成本
     */
    @Select("SELECT COALESCE(SUM(cost_amount), 0) FROM orders WHERE status = 'PAID' AND DATE(create_time) = #{date}")
    BigDecimal getDailyCost(@Param("date") String date);

    /**
     * 查询指定月份的总成本
     */
    @Select("SELECT COALESCE(SUM(cost_amount), 0) FROM orders WHERE status = 'PAID' AND DATE_FORMAT(create_time, '%Y-%m') = #{yearMonth}")
    BigDecimal getMonthlyCost(@Param("yearMonth") String yearMonth);

    /**
     * 查询热销商品（按销量排序）
     */
    @Select("SELECT oi.product_id, oi.product_name, SUM(oi.quantity) AS total_quantity, SUM(oi.subtotal) AS total_amount " +
            "FROM order_item oi INNER JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.status = 'PAID' AND DATE(o.create_time) = #{date} " +
            "GROUP BY oi.product_id, oi.product_name ORDER BY total_quantity DESC LIMIT #{limit}")
    List<com.supermarket.dto.TopProductDTO> getTopProducts(@Param("date") String date, @Param("limit") int limit);
}