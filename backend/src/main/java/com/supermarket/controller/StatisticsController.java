package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.TopProductDTO;
import com.supermarket.mapper.PurchaseOrderMapper;
import com.supermarket.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    /**
     * 日营业额
     *
     * @param date 日期，格式 yyyy-MM-dd，默认当天
     */
    @GetMapping("/daily")
    public Result<Map<String, Object>> dailyRevenue(@RequestParam(required = false) String date) {
        if (date == null) {
            date = LocalDate.now().toString();
        }
        return Result.success(statisticsService.getDailyRevenue(date));
    }

    /**
     * 月营业额
     *
     * @param year  年份，默认当前年
     * @param month 月份，默认当前月
     */
    @GetMapping("/monthly")
    public Result<Map<String, Object>> monthlyRevenue(@RequestParam(required = false) Integer year,
                                                      @RequestParam(required = false) Integer month) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        return Result.success(statisticsService.getMonthlyRevenue(year, month));
    }

    /**
     * 热销商品TopN
     *
     * @param date  日期，格式 yyyy-MM-dd，默认当天
     * @param limit 数量，默认10
     */
    @GetMapping("/top-products")
    public Result<List<TopProductDTO>> topProducts(@RequestParam(required = false) String date,
                                                   @RequestParam(defaultValue = "10") int limit) {
        if (date == null) {
            date = LocalDate.now().toString();
        }
        return Result.success(statisticsService.getTopProducts(date, limit));
    }

    /**
     * 采购统计（日采购额、月采购额）
     */
    @GetMapping("/purchase")
    public Result<Map<String, BigDecimal>> purchaseStats(@RequestParam(required = false) String date) {
        if (date == null) {
            date = LocalDate.now().toString();
        }
        String yearMonth = date.substring(0, 7);

        BigDecimal dailyPurchase = purchaseOrderMapper.getDailyPurchaseAmount(date);
        BigDecimal monthlyPurchase = purchaseOrderMapper.getMonthlyPurchaseAmount(yearMonth);

        Map<String, BigDecimal> map = new HashMap<>();
        map.put("dailyPurchase", dailyPurchase);
        map.put("monthlyPurchase", monthlyPurchase);
        return Result.success(map);
    }
}