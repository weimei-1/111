package com.supermarket.service;

import com.supermarket.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 查询所有分类，支持按名称搜索
     */
    List<Category> getAll(String name);

    /**
     * 根据ID查询分类
     */
    Category getById(Long id);

    /**
     * 新增分类
     */
    void add(Category category);

    /**
     * 修改分类
     */
    void update(Category category);

    /**
     * 删除分类
     */
    void delete(Long id);
}