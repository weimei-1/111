package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/list")
    public Result<List<Supplier>> list(@RequestParam(required = false) String keyword) {
        return Result.success(supplierService.getAll(keyword));
    }

    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Supplier supplier) {
        supplierService.add(supplier);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Supplier supplier) {
        supplierService.update(supplier);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.success("删除成功", null);
    }
}