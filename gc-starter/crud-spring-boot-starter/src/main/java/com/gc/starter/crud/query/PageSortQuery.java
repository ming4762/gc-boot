package com.gc.starter.crud.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页排序查询
 * @author shizhongming
 * 2021/4/24 6:47 下午
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PageSortQuery extends CommonQuery {

    private static final long serialVersionUID = 401040997642894963L;


    /**
     * 排序方向已逗号分隔
     */
    private String sortOrder;

    /**
     * 排序字段 已逗号分隔
     */
    private String sortName;

    /**
     * 每页条数
     */
    private Integer limit;

    /**
     * 起始记录数
     */
    private Integer offset = 0;

    /**
     * 页数
     */
    private Integer page;

    /**
     * 关键字查询
     */
    private String keyword;
}
