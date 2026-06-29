package com.supermarket.service;

import com.supermarket.dto.TopProductDTO;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取日营业额（含订单数）
     *
     * @param date 日期，格式 yyyy-MM-dd
     */
    Map<String, Object> getDailyRevenue(String date);

    /**
     * 获取月营业额
     *
     * @param year  年份
     * @param month 月份
     */
    Map<String, Object> getMonthlyRevenue(int year, int month);

    /**
     * 获取热销商品TopN
     *
     * @param date  日期，格式 yyyy-MM-dd
     * @param limit 数量
     */
    List<TopProductDTO> getTopProducts(String date, int limit);
}