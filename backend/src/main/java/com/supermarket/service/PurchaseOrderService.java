package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.entity.PurchaseOrder;

public interface PurchaseOrderService {

    PageResult<PurchaseOrder> getList(int page, int pageSize, String orderNo, Integer status);

    PurchaseOrder getById(Long id);

    void add(PurchaseOrder purchaseOrder);

    void update(PurchaseOrder purchaseOrder);

    void delete(Long id);

    void stockIn(Long id);
}