package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.MemberLevel;
import com.supermarket.mapper.MemberLevelMapper;
import com.supermarket.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class MemberLevelServiceImpl implements MemberLevelService {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Override
    public List<MemberLevel> getAll(String name) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(MemberLevel::getName, name);
        }
        wrapper.orderByAsc(MemberLevel::getSort);
        return memberLevelMapper.selectList(wrapper);
    }

    @Override
    public MemberLevel getById(Long id) {
        return memberLevelMapper.selectById(id);
    }

    @Override
    public void add(MemberLevel memberLevel) {
        if (memberLevel.getSort() == null) {
            memberLevel.setSort(0);
        }
        if (memberLevel.getStatus() == null) {
            memberLevel.setStatus(1);
        }
        memberLevelMapper.insert(memberLevel);
    }

    @Override
    public void update(MemberLevel memberLevel) {
        memberLevelMapper.updateById(memberLevel);
    }

    @Override
    public void delete(Long id) {
        memberLevelMapper.deleteById(id);
    }
}