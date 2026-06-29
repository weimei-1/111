package com.supermarket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Member;
import com.supermarket.entity.User;
import com.supermarket.mapper.MemberMapper;
import com.supermarket.mapper.OrderItemMapper;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.mapper.UserMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/orders")
    public void exportOrders(@RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             HttpServletResponse response) throws Exception {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (startDate != null) {
            wrapper.ge(Order::getCreateTime, sdf.parse(startDate + " 00:00:00"));
        }
        if (endDate != null) {
            wrapper.le(Order::getCreateTime, sdf.parse(endDate + " 23:59:59"));
        }
        wrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = orderMapper.selectList(wrapper);

        // 批量加载会员和用户名称
        Map<Long, String> memberMap = new HashMap<>();
        Map<Long, String> userMap = new HashMap<>();
        List<Member> members = memberMapper.selectList(null);
        List<User> users = userMapper.selectList(null);
        for (Member m : members) memberMap.put(m.getId(), m.getName());
        for (User u : users) userMap.put(u.getId(), u.getRealName());

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("销售报表");

        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(createFont(wb, true, (short) 12));
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        String[] headers = {"订单号", "收银员", "会员", "商品数量", "订单总额", "优惠金额", "实付金额", "成本金额", "利润", "支付方式", "状态", "下单时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getOrderNo());

            String cashierName = userMap.getOrDefault(order.getCashierId(), "");
            row.createCell(1).setCellValue(cashierName);

            String memberName = memberMap.getOrDefault(order.getMemberId(), "");
            row.createCell(2).setCellValue(memberName);

            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            int totalQty = items.stream().mapToInt(OrderItem::getQuantity).sum();
            row.createCell(3).setCellValue(totalQty);

            BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal pay = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
            BigDecimal cost = order.getCostAmount() != null ? order.getCostAmount() : BigDecimal.ZERO;
            BigDecimal profit = pay.subtract(cost);

            row.createCell(4).setCellValue(total.doubleValue());
            row.createCell(5).setCellValue(discount.doubleValue());
            row.createCell(6).setCellValue(pay.doubleValue());
            row.createCell(7).setCellValue(cost.doubleValue());
            row.createCell(8).setCellValue(profit.doubleValue());

            String payType = order.getPayType();
            row.createCell(9).setCellValue("CASH".equals(payType) ? "现金" : "ALIPAY".equals(payType) ? "支付宝" : "WECHAT".equals(payType) ? "微信" : payType);

            String status = order.getStatus();
            row.createCell(10).setCellValue("PAID".equals(status) ? "已支付" : "REFUNDED".equals(status) ? "已退款" : "CANCELED".equals(status) ? "已取消" : status);

            Cell dateCell = row.createCell(11);
            dateCell.setCellValue(order.getCreateTime());
            dateCell.setCellStyle(dateStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("销售报表.xlsx", "UTF-8"));
        OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
        wb.close();
    }

    private Font createFont(Workbook wb, boolean bold, short size) {
        Font font = wb.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints(size);
        font.setColor(IndexedColors.WHITE.getIndex());
        return font;
    }
}