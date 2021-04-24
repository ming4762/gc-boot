package com.gc.starter.crud.query;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用查询
 * @author shizhongming
 * 2021/4/24 6:45 下午
 * @since 2.0
 */
@Getter
@Setter
public class CommonQuery implements Serializable {
    private static final long serialVersionUID = 25607615568253403L;

    private Map<String, Object> parameter = new HashMap<>(0);
}
