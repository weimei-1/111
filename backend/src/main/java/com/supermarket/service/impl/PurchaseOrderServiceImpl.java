package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.PageResult;
import com.supermarket.entity.Product;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.entity.PurchaseOrderItem;
import com.supermarket.entity.Supplier;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.PurchaseOrderItemMapper;
import com.supermarket.mapper.PurchaseOrderMapper;
import com.supermarket.mapper.SupplierMapper;
import com.supermarket.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public PageResult<PurchaseOrder> getList(int page, int pageSize, String orderNo, Integer status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(PurchaseOrder::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);

        IPage<PurchaseOrder> iPage = purchaseOrderMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // 加载明细和供应商名称
        for (PurchaseOrder po : iPage.getRecords()) {
            loadItems(po);
            loadSupplierName(po);
        }

        return new PageResult<>(iPage.getTotal(), page, pageSize, iPage.getRecords());
    }

    @Override
    public PurchaseOrder getById(Long id) {
        PurchaseOrder po = purchaseOrderMapper.selectById(id);
        if (po != null) {
            loadItems(po);
        }
        return po;
    }

    @Override
    public void add(PurchaseOrder purchaseOrder) {
        // 生成采购单号：CG + yyyyMMdd + 4位随机数
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        purchaseOrder.setOrderNo("CG" + datePart + randomPart);

        if (purchaseOrder.getStatus() == null) {
            purchaseOrder.setStatus(0);
        }

        // 计算总金额
        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = purchaseOrder.getItems();
        if (items != null) {
            for (PurchaseOrderItem item : items) {
                BigDecimal subtotal = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                item.setSubtotal(subtotal);
                total = total.add(subtotal);
            }
        }
        purchaseOrder.setTotalAmount(total);

        purchaseOrderMapper.insert(purchaseOrder);

        // 保存明细
        if (items != null) {
            for (PurchaseOrderItem item : items) {
                item.setPurchaseOrderId(purchaseOrder.getId());
                purchaseOrderItemMapper.insert(item);
            }
        }
    }

    @Override
    public void update(PurchaseOrder purchaseOrder) {
        // 只更新头部信息（供应商、备注），不重新计算明细
        PurchaseOrder old = purchaseOrderMapper.selectById(purchaseOrder.getId());
        if (old != null) {
            old.setSupplierId(purchaseOrder.getSupplierId());
            old.setRemark(purchaseOrder.getRemark());
            purchaseOrderMapper.updateById(old);
        }
    }

    @Override
    public void delete(Long id) {
        // 删除明细
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getPurchaseOrderId, id);
        purchaseOrderItemMapper.delete(wrapper);
        // 删除主表
        purchaseOrderMapper.deleteById(id);
    }

    @Override
    public void stockIn(Long id) {
        PurchaseOrder po = purchaseOrderMapper.selectById(id);
        if (po == null) {
            throw new IllegalArgumentException("采购单不存在");
        }
        if (po.getStatus() != 0) {
            throw new IllegalArgumentException("只有待入库的采购单才能入库");
        }

        // 加载明细
        loadItems(po);

        // 更新商品库存和成本价
        for (PurchaseOrderItem item : po.getItems()) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                // 更新成本价为最新采购价
                product.setCostPrice(item.getPurchasePrice());
                productMapper.updateById(product);
            }
        }

        // 更新采购单状态为已入库
        po.setStatus(1);
        purchaseOrderMapper.updateById(po);
    }

    private void loadSupplierName(PurchaseOrder po) {
        if (po.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(po.getSupplierId());
            if (supplier != null) {
                po.setSupplierName(supplier.getName());
            }
        }
    }

    private void loadItems(PurchaseOrder po) {
        LambdaQueryWrapper<PurchaseOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseOrderItem::getPurchaseOrderId, po.getId());
        po.setItems(purchaseOrderItemMapper.selectList(itemWrapper));
    }
}