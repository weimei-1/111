package com.supermarket.controller;

import com.supermarket.common.PageResult;
import com.supermarket.common.Result;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/list")
    public Result<PageResult<PurchaseOrder>> list(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) String orderNo,
                                                   @RequestParam(required = false) Integer status) {
        return Result.success(purchaseOrderService.getList(page, pageSize, orderNo, status));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> getById(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody PurchaseOrder purchaseOrder) {
        purchaseOrderService.add(purchaseOrder);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody PurchaseOrder purchaseOrder) {
        purchaseOrderService.update(purchaseOrder);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/stock-in/{id}")
    public Result<?> stockIn(@PathVariable Long id) {
        purchaseOrderService.stockIn(id);
        return Result.success("入库成功", null);
    }
}