package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.entity.Member;

/**
 * 会员服务接口
 */
public interface MemberService {

    /**
     * 分页查询会员列表
     */
    PageResult<Member> getMemberList(int page, int pageSize, String keyword);

    /**
     * 根据ID查询会员
     */
    Member getById(Long id);

    /**
     * 根据手机号查询会员
     */
    Member getByPhone(String phone);

    /**
     * 新增会员
     */
    void add(Member member);

    /**
     * 修改会员
     */
    void update(Member member);

    /**
     * 删除会员
     */
    void delete(Long id);
}