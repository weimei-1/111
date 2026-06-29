package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Category;
import com.supermarket.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类（不分页），支持按名称搜索
     */
    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) String name) {
        return Result.success(categoryService.getAll(name));
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.success("新增成功", null);
    }

    /**
     * 修改分类
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success("修改成功", null);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}