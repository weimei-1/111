package com.supermarket.controller;

import com.supermarket.common.PageResult;
import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/list")
    public Result<PageResult<Product>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(productService.getProductList(page, pageSize, name, categoryId));
    }

    /**
     * 库存预警列表（返回全部预警商品，不分页）
     */
    @GetMapping("/warning")
    public Result<List<Product>> warningStock() {
        PageResult<Product> pageResult = productService.getWarningStockList(1, 9999);
        return Result.success(pageResult.getList());
    }

    /**
     * 根据条码查询商品
     */
    @GetMapping("/barcode/{barcode}")
    public Result<Product> getByBarcode(@PathVariable String barcode) {
        Product product = productService.getByBarcode(barcode);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 根据ID查询商品
     */
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /**
     * 新增商品
     */
    @PostMapping("")
    public Result<?> add(@RequestBody Product product) {
        productService.add(product);
        return Result.success("新增成功", null);
    }

    /**
     * 修改商品
     */
    @PutMapping("")
    public Result<?> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success("修改成功", null);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("删除成功", null);
    }
}