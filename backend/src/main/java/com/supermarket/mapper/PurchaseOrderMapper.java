package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    /** 查询指定日期的已入库采购总额 */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM purchase_order WHERE status = 1 AND DATE(create_time) = #{date}")
    BigDecimal getDailyPurchaseAmount(@Param("date") String date);

    /** 查询指定月份的已入库采购总额 */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM purchase_order WHERE status = 1 AND DATE_FORMAT(create_time, '%Y-%m') = #{yearMonth}")
    BigDecimal getMonthlyPurchaseAmount(@Param("yearMonth") String yearMonth);
}