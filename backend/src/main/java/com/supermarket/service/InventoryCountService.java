package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.entity.InventoryCount;

public interface InventoryCountService {

    PageResult<InventoryCount> getList(int page, int pageSize, String countNo, Integer status);

    InventoryCount getById(Long id);

    void add(InventoryCount inventoryCount);

    void update(InventoryCount inventoryCount);

    void delete(Long id);

    void complete(Long id);

    void audit(Long id);
}