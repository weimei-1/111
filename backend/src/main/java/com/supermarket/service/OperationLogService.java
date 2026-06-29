package com.supermarket.service;

import com.supermarket.common.PageResult;
import com.supermarket.entity.OperationLog;

public interface OperationLogService {

    PageResult<OperationLog> getList(int page, int pageSize, String module, String action);

    void save(OperationLog log);

    void deleteByIds(java.util.List<Long> ids);
}