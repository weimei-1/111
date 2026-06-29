package com.supermarket.controller;

import com.supermarket.common.PageResult;
import com.supermarket.common.Result;
import com.supermarket.entity.Member;
import com.supermarket.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员管理控制器
 */
@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 分页查询会员列表
     */
    @GetMapping("/list")
    public Result<PageResult<Member>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String keyword) {
        return Result.success(memberService.getMemberList(page, pageSize, keyword));
    }

    /**
     * 根据ID查询会员
     */
    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    /**
     * 根据手机号查询会员
     */
    @GetMapping("/phone/{phone}")
    public Result<Member> getByPhone(@PathVariable String phone) {
        Member member = memberService.getByPhone(phone);
        if (member == null) {
            return Result.error("会员不存在");
        }
        return Result.success(member);
    }

    /**
     * 新增会员
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Member member) {
        memberService.add(member);
        return Result.success("新增成功", null);
    }

    /**
     * 修改会员
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Member member) {
        memberService.update(member);
        return Result.success("修改成功", null);
    }

    /**
     * 删除会员
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        memberService.delete(id);
        return Result.success("删除成功", null);
    }
}