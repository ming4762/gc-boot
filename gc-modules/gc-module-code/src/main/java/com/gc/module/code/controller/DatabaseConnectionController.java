package com.gc.module.code.controller;

import com.gc.common.base.message.Result;
import com.gc.module.code.model.DatabaseConnectionPO;
import com.gc.module.code.service.DatabaseConnectionService;
import com.gc.starter.crud.controller.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据库连接信息controller层
 * @author jackson
 * 2020/1/21 10:13 下午
 */
@RestController
@RequestMapping("db/connect")
public class DatabaseConnectionController extends BaseController<DatabaseConnectionService, DatabaseConnectionPO> {

    @RequestMapping("list")
    @PreAuthorize("hasRole('abc')")
//    @PreAuthorize("hasPermission('abc', '123') or hasPermission('user', '123')")
//    @PreAuthorize("hasPermission('abc', '123')")
    @Override
    protected Result<Object> list(Map<String, Object> parameter) {
        return super.list(parameter);
    }
}