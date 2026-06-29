package com.supermarket.service.impl;

import com.supermarket.dto.TopProductDTO;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Map<String, Object> getDailyRevenue(String date) {
        BigDecimal revenue = orderMapper.getDailyRevenue(date);
        BigDecimal cost = orderMapper.getDailyCost(date);
        Long orderCount = orderMapper.getDailyOrderCount(date);
        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("revenue", revenue);
        result.put("cost", cost);
        result.put("profit", revenue.subtract(cost));
        result.put("orderCount", orderCount);
        return result;
    }

    @Override
    public Map<String, Object> getMonthlyRevenue(int year, int month) {
        String yearMonth = String.format("%04d-%02d", year, month);
        BigDecimal revenue = orderMapper.getMonthlyRevenue(yearMonth);
        BigDecimal cost = orderMapper.getMonthlyCost(yearMonth);
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("revenue", revenue);
        result.put("cost", cost);
        result.put("profit", revenue.subtract(cost));
        return result;
    }

    @Override
    public List<TopProductDTO> getTopProducts(String date, int limit) {
        return orderMapper.getTopProducts(date, limit);
    }
}