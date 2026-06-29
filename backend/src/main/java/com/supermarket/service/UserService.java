package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.entity.User;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Map containing token and user info
     */
    Map<String, Object> login(String username, String password);

    /**
     * 分页查询用户列表
     */
    PageResult<User> getUserList(int page, int pageSize, String keyword);

    /**
     * 根据ID查询用户
     */
    User getById(Long id);

    /**
     * 新增用户
     */
    void add(User user);

    /**
     * 修改用户
     */
    void update(User user);

    /**
     * 删除用户
     */
    void delete(Long id);
}