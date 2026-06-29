package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.PageResult;
import com.supermarket.entity.InventoryCount;
import com.supermarket.entity.InventoryCountItem;
import com.supermarket.entity.Product;
import com.supermarket.mapper.InventoryCountItemMapper;
import com.supermarket.mapper.InventoryCountMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.InventoryCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class InventoryCountServiceImpl implements InventoryCountService {

    @Autowired
    private InventoryCountMapper inventoryCountMapper;

    @Autowired
    private InventoryCountItemMapper itemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResult<InventoryCount> getList(int page, int pageSize, String countNo, Integer status) {
        LambdaQueryWrapper<InventoryCount> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(countNo)) {
            wrapper.like(InventoryCount::getCountNo, countNo);
        }
        if (status != null) {
            wrapper.eq(InventoryCount::getStatus, status);
        }
        wrapper.orderByDesc(InventoryCount::getCreateTime);
        IPage<InventoryCount> iPage = inventoryCountMapper.selectPage(new Page<>(page, pageSize), wrapper);

        for (InventoryCount ic : iPage.getRecords()) {
            loadItems(ic);
        }
        return new PageResult<>(iPage.getTotal(), page, pageSize, iPage.getRecords());
    }

    @Override
    public InventoryCount getById(Long id) {
        InventoryCount ic = inventoryCountMapper.selectById(id);
        if (ic != null) loadItems(ic);
        return ic;
    }

    @Override
    public void add(InventoryCount inventoryCount) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        inventoryCount.setCountNo("PD" + datePart + randomPart);
        if (inventoryCount.getStatus() == null) inventoryCount.setStatus(0);
        if (inventoryCount.getCountDate() == null) inventoryCount.setCountDate(new java.util.Date());

        // 计算差异
        List<InventoryCountItem> items = inventoryCount.getItems();
        if (items != null) {
            for (InventoryCountItem item : items) {
                item.setDifference(item.getActualStock() - item.getBookStock());
            }
        }

        inventoryCountMapper.insert(inventoryCount);

        if (items != null) {
            for (InventoryCountItem item : items) {
                item.setCountId(inventoryCount.getId());
                itemMapper.insert(item);
            }
        }
    }

    @Override
    public void update(InventoryCount inventoryCount) {
        InventoryCount old = inventoryCountMapper.selectById(inventoryCount.getId());
        if (old != null) {
            old.setRemark(inventoryCount.getRemark());
            inventoryCountMapper.updateById(old);
        }
    }

    @Override
    public void delete(Long id) {
        LambdaQueryWrapper<InventoryCountItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCountItem::getCountId, id);
        itemMapper.delete(wrapper);
        inventoryCountMapper.deleteById(id);
    }

    @Override
    public void complete(Long id) {
        InventoryCount ic = inventoryCountMapper.selectById(id);
        if (ic == null) throw new IllegalArgumentException("盘点单不存在");
        if (ic.getStatus() != 0) throw new IllegalArgumentException("只有待盘点状态才能完成盘点");
        ic.setStatus(1);
        inventoryCountMapper.updateById(ic);
    }

    @Override
    public void audit(Long id) {
        InventoryCount ic = inventoryCountMapper.selectById(id);
        if (ic == null) throw new IllegalArgumentException("盘点单不存在");
        if (ic.getStatus() != 1) throw new IllegalArgumentException("只有已完成状态才能审核");

        loadItems(ic);

        // 审核通过：按实际库存更新商品库存
        for (InventoryCountItem item : ic.getItems()) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(item.getActualStock());
                productMapper.updateById(product);
            }
        }

        ic.setStatus(2);
        inventoryCountMapper.updateById(ic);
    }

    private void loadItems(InventoryCount ic) {
        LambdaQueryWrapper<InventoryCountItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCountItem::getCountId, ic.getId());
        ic.setItems(itemMapper.selectList(wrapper));
    }
}