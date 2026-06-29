package com.supermarket.controller;

import com.supermarket.common.PageResult;
import com.supermarket.common.Result;
import com.supermarket.entity.InventoryCount;
import com.supermarket.service.InventoryCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-count")
public class InventoryCountController {

    @Autowired
    private InventoryCountService inventoryCountService;

    @GetMapping("/list")
    public Result<PageResult<InventoryCount>> list(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) String countNo,
                                                    @RequestParam(required = false) Integer status) {
        return Result.success(inventoryCountService.getList(page, pageSize, countNo, status));
    }

    @GetMapping("/{id}")
    public Result<InventoryCount> getById(@PathVariable Long id) {
        return Result.success(inventoryCountService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody InventoryCount inventoryCount) {
        inventoryCountService.add(inventoryCount);
        return Result.success("新增成功", null);
    }

    @PutMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id) {
        inventoryCountService.complete(id);
        return Result.success("盘点完成", null);
    }

    @PutMapping("/audit/{id}")
    public Result<?> audit(@PathVariable Long id) {
        inventoryCountService.audit(id);
        return Result.success("审核通过，库存已更新", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        inventoryCountService.delete(id);
        return Result.success("删除成功", null);
    }
}