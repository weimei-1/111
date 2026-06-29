package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.entity.Product;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 分页查询商品
     */
    PageResult<Product> getProductList(int page, int pageSize, String keyword, Long categoryId);

    /**
     * 根据ID查询商品
     */
    Product getById(Long id);

    /**
     * 根据条码查询商品
     */
    Product getByBarcode(String barcode);

    /**
     * 查询库存预警商品列表
     */
    PageResult<Product> getWarningStockList(int page, int pageSize);

    /**
     * 新增商品
     */
    void add(Product product);

    /**
     * 修改商品
     */
    void update(Product product);

    /**
     * 删除商品
     */
    void delete(Long id);
}