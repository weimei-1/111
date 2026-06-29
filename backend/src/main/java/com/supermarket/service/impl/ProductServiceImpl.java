package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.PageResult;
import com.supermarket.entity.Product;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品服务实现类
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResult<Product> getProductList(int page, int pageSize, String keyword, Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword)
                    .or()
                    .like(Product::getBarcode, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> iPage = productMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(iPage.getTotal(), page, pageSize, iPage.getRecords());
    }

    @Override
    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public Product getByBarcode(String barcode) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getBarcode, barcode);
        return productMapper.selectOne(wrapper);
    }

    @Override
    public PageResult<Product> getWarningStockList(int page, int pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("stock <= warning_stock")
                .eq(Product::getStatus, 1);
        wrapper.orderByAsc(Product::getStock);

        IPage<Product> iPage = productMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(iPage.getTotal(), page, pageSize, iPage.getRecords());
    }

    @Override
    public void add(Product product) {
        // 检查条码是否已存在
        if (StringUtils.hasText(product.getBarcode())) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getBarcode, product.getBarcode());
            if (productMapper.selectCount(wrapper) > 0) {
                throw new IllegalArgumentException("商品条码已存在");
            }
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        productMapper.insert(product);
    }

    @Override
    public void update(Product product) {
        productMapper.updateById(product);
    }

    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
    }
}