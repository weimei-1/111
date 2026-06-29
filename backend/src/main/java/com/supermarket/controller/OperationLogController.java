package com.supermarket.controller;

import com.supermarket.common.PageResult;
import com.supermarket.common.Result;
import com.supermarket.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<PageResult<?>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String module,
                                       @RequestParam(required = false) String action) {
        return Result.success(operationLogService.getList(page, pageSize, module, action));
    }

    @DeleteMapping("/batch")
    public Result<Void> remove(@RequestBody List<Long> ids) {
        operationLogService.deleteByIds(ids);
        return Result.success();
    }
}