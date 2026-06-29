package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.PageResult;
import com.supermarket.entity.Member;
import com.supermarket.mapper.MemberMapper;
import com.supermarket.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 会员服务实现类
 */
@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public PageResult<Member> getMemberList(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Member::getName, keyword)
                    .or()
                    .like(Member::getPhone, keyword);
        }
        wrapper.orderByDesc(Member::getCreateTime);

        IPage<Member> iPage = memberMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(iPage.getTotal(), page, pageSize, iPage.getRecords());
    }

    @Override
    public Member getById(Long id) {
        return memberMapper.selectById(id);
    }

    @Override
    public Member getByPhone(String phone) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, phone);
        return memberMapper.selectOne(wrapper);
    }

    @Override
    public void add(Member member) {
        // 检查手机号是否已存在
        if (StringUtils.hasText(member.getPhone())) {
            LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Member::getPhone, member.getPhone());
            if (memberMapper.selectCount(wrapper) > 0) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }
        if (member.getPoints() == null) {
            member.setPoints(0);
        }
        if (member.getLevel() == null) {
            member.setLevel("普通会员");
        }
        if (member.getTotalSpent() == null) {
            member.setTotalSpent(BigDecimal.ZERO);
        }
        memberMapper.insert(member);
    }

    @Override
    public void update(Member member) {
        memberMapper.updateById(member);
    }

    @Override
    public void delete(Long id) {
        memberMapper.deleteById(id);
    }
}