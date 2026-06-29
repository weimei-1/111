package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Promotion;
import com.supermarket.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotion")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/list")
    public Result<List<Promotion>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(promotionService.getAll(keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Promotion> getById(@PathVariable Long id) {
        return Result.success(promotionService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Promotion promotion) {
        promotionService.add(promotion);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Promotion promotion) {
        promotionService.update(promotion);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return Result.success("删除成功", null);
    }
}