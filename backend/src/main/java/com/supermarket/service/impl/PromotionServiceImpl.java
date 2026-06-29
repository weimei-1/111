package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.Promotion;
import com.supermarket.mapper.PromotionMapper;
import com.supermarket.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionMapper promotionMapper;

    @Override
    public List<Promotion> getAll(String keyword, Integer status) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Promotion::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Promotion::getStatus, status);
        }
        wrapper.orderByDesc(Promotion::getCreateTime);
        return promotionMapper.selectList(wrapper);
    }

    @Override
    public Promotion getById(Long id) {
        return promotionMapper.selectById(id);
    }

    @Override
    public void add(Promotion promotion) {
        if (promotion.getStatus() == null) {
            promotion.setStatus(0);
        }
        promotionMapper.insert(promotion);
    }

    @Override
    public void update(Promotion promotion) {
        promotionMapper.updateById(promotion);
    }

    @Override
    public void delete(Long id) {
        promotionMapper.deleteById(id);
    }
}