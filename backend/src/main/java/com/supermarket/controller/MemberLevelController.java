package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.MemberLevel;
import com.supermarket.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-level")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    @GetMapping("/list")
    public Result<List<MemberLevel>> list(@RequestParam(required = false) String name) {
        return Result.success(memberLevelService.getAll(name));
    }

    @GetMapping("/{id}")
    public Result<MemberLevel> getById(@PathVariable Long id) {
        return Result.success(memberLevelService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody MemberLevel memberLevel) {
        memberLevelService.add(memberLevel);
        return Result.success("新增成功", null);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody MemberLevel memberLevel) {
        memberLevelService.update(memberLevel);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        memberLevelService.delete(id);
        return Result.success("删除成功", null);
    }
}